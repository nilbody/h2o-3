package hex;

import java.util.Map;

import water.Futures;
import water.H2O;
import water.H2O.H2OCountedCompleter;
import water.Job;
import water.Key;
import water.Lockable;
import water.fvec.Frame;
import water.util.IcedHashMap;
import water.util.IcedLong;
import water.util.Log;
import water.util.PojoUtils;

/**
 * A Grid of Models Used to explore Model hyper-parameter space.  Lazily filled in, this object
 * represents the potentially infinite variety of hyperparameters of a given model & dataset.
 *
 * One subclass per kind of Model, e.g. KMeans or GLM or GBM or DL.  The Grid tracks Models and
 * their hyperparameters, and will allow discovery of existing Models by hyperparameter, or building
 * Models on demand by hyperparameter.  The Grid can manage a (simplistic) hyperparameter search
 * space.
 *
 * The external Grid API uses a HashMap<String,Object> to describe a set of hyperparameter values,
 * where the String is a valid field name in the corresponding Model.Parameter, and the Object is
 * the field value (boxed as needed).
 *
 * The Grid implementation treats all hyperparameters as double values internally, indexed by a
 * simple number.  A complete set of hyper parameters is thus a {@code double[]}, and a set of
 * search parameters a {@code double[][]}.  The subclasses of Grid will need to convert between
 * these two formats.
 *
 * E.g. KMeansGrid will convert the initial center selection field "_init" Enum to and from a simple
 * double value internally.
 *
 * @param <MP> type of model build parameters
 * @param <G>  self-type of actual Grid implementation
 */
// FIXME should be abstract but REST API layer requires not abstract
// FIXME should follow model build structure with INPUT and OUTPUT
public /*abstract*/ class Grid<MP extends Model.Parameters, G extends Grid<MP, G>>
    extends Lockable<G> {

  /**
   * The training frame for this grid of models.
   */
  protected final Frame _fr;

  /**
   * A cache of double[] hyper-parameters mapping to Models.
   */
  final IcedHashMap<IcedLong, Key<Model>> _cache = new IcedHashMap<>();

  /**
   * Used model parameters for this grid search.
   */
  final MP _params;

  /**
   * Names of used hyper parameters for this grid search.
   */
  final String[] _hyper_names;

  public Grid(Key key, Frame fr, MP params, String[] hyperNames) {
    super(key);
    _fr = fr;
    _params = params != null ? (MP) params.clone() : null;
    _hyper_names = hyperNames;
  }

  /**
   * @return Model name
   */
  protected /*abstract*/ String modelName() {
    throw H2O.fail();
  }

  /**
   * Ask the Grid for a suggested next hyperparameter value, given an existing Model as a starting
   * point and the complete set of hyperparameter limits. Returning a NaN signals there is no next
   * suggestion, which is reasonable if the obvious "next" value does not exist (e.g. exhausted all
   * possibilities of an enum).  It is OK if a Model for the suggested value already exists; this
   * will be checked before building any model.
   *
   * @param h           The h-th hyperparameter
   * @param m           A model to act as a starting point
   * @param hyperLimits Upper bounds for this search
   * @return Suggested next value for hyperparameter h or NaN if no next value
   */
  protected /*abstract*/ double suggestedNextHyperValue(int h, Model m, double[] hyperLimits) {
    throw H2O.fail();
  }

  /**
   * @return The data frame used to train all these models.  All models are trained on the same data
   * frame, but might be validated on multiple different frames.
   */
  public Frame trainingFrame() {
    return _fr;
  }

  /**
   * @return Factory method to return the grid for a particular modeling class and frame.
   */
  protected static Key<Grid> keyName(String modelName, Frame fr) {
    if (fr._key == null) {
      throw new IllegalArgumentException("The frame being grid-searched over must have a Key");
    }
    return Key.make("Grid_" + modelName + "_" + fr._key.toString());
  }

  /**
   * @param params A set of hyper parameter values
   * @return A model run with these parameters, or null if the model does not exist.
   */
  public Key<Model> model(MP params) {
    long checksum = params.checksum_impl();
    return _cache.get(IcedLong.valueOf(checksum));
  }

  public Key<Model>[] getModels() {
    return _cache.values().toArray(new Key[_cache.size()]);
  }

  /**
   * Create a new model builder for given parameters.
   *
   * @param params model builder parameters, it is private copy provided for a model builder
   */
  protected /*abstract*/ ModelBuilder createBuilder(MP params) {
    throw H2O.fail();
  }

  ;

  /**
   * @param parms Model parameters
   * @return Gridable parameters pulled out of the parms
   */
  public /*abstract*/ Object[] getHyperValues(MP parms) {
    Object[] result = new Object[_hyper_names.length];
    for (int i = 0; i < _hyper_names.length; i++) {
      result[i] = PojoUtils.getFieldValue(parms, _hyper_names[i]);
    }
    return result;
  }

  /** Returns an array of used hyper parameters.
   *
   * @return  names of hyper parameters used in this hyper search
   */
  public String[] getHyperNames() {
    return _hyper_names;
  }

  /**
   * @param hypers A set of hyper parameter values
   * @return A Future of a model run with these parameters, typically built on demand and not cached
   * - expected to be an expensive operation.  If the model in question is "in progress", a 2nd
   * build will NOT be kicked off. This is a non-blocking call.
   */
  private ModelBuilder startBuildModel(MP params) {
    if (model(params) != null) {
      return null;
    }
    ModelBuilder mb = createBuilder(params);
    mb.trainModel();
    return mb;
  }

  /**
   * @param hypers A set of hyper parameter values
   * @return A model run with these parameters, typically built on demand and cached - expected to
   * be an expensive operation.  If the model in question is "in progress", a 2nd build will NOT be
   * kicked off. This is a blocking call.
   */
  private Model buildModel(final MP params) {
    Key<Model> key = model(params);
    // It was already built
    if (key != null) {
      return key.get();
    }
    // FIXME: get checksum here since model builder will modify instance of params!!!
    long checksum = params.checksum_impl();
    // Build a new model
    // FIXME: blocking build
    // FIXME: should not crash the Grid search
    Model m = (Model) (startBuildModel(params).get());
    _cache.put(IcedLong.valueOf(checksum), m._key);
    return m;
  }

  /**
   * @param params      Default parameters for grid search builder
   * @param hyperParams A set of arrays of hyper parameter values, used to specify a simple
   *                    fully-filled-in grid search.
   * @return GridSearch Job, with models run with these parameters, built as needed - expected to be
   * an expensive operation.  If the models in question are "in progress", a 2nd build will NOT be
   * kicked off.  This is a non-blocking call.
   */
  public GridSearch startGridSearch(final MP params,
                                    final Map<String, Object[]> hyperParams,
                                    final ModelParametersBuilderFactory<MP> paramsBuilderFactory) {
    return new GridSearch(_key, params, hyperParams, paramsBuilderFactory).start();
  }

  public GridSearch startGridSearch(final MP params,
                                    final Map<String, Object[]> hyperParams) {
    return startGridSearch(params, hyperParams, new SimpleParametersBuilderFactory<MP>());
  }

  // Cleanup models and grid
  @Override
  protected Futures remove_impl(Futures fs) {
    for (Key<Model> k : _cache.values()) {
      k.remove(fs);
    }
    _cache.clear();
    return fs;
  }

  /**
   * Grid search job.
   *
   * This job triggers sub-jobs for each point in hyper space.
   * It produces <code>Grid</code> which contains a list of build models.
   */
  public final class GridSearch extends Job<Grid> {

    /**
     * Grid search parameters for this job. It is used only locally to fire job builders.
     */
    final transient Map<String, Object[]> _hyperParams;
    /**
     * Total number of models produced by this grid search.
     */
    final int _totalModels;
    /**
     * Initial model builder parameters.
     */
    final MP _params;
    /**
     * Names of passed grid parameters
     */
    final String[] _hyperParamNames;
    /**
     * Parameters builder factory.
     */
    final transient ModelParametersBuilderFactory<MP> _paramsBuilderFactory;

    GridSearch(Key gkey, MP params, Map<String, Object[]> hyperParams,
               ModelParametersBuilderFactory<MP> paramsBuilderFactory) {
      super(gkey, modelName() + " Grid Search");
      _params = params;
      _hyperParams = hyperParams;
      _paramsBuilderFactory = paramsBuilderFactory;

      // Count of models in this search
      _totalModels = computeSizeOfHyperSpace();
      _hyperParamNames = hyperParams.keySet().toArray(new String[]{});
      assert _hyperParamNames.length
             == _hyper_names.length : "Something is wrong with number of gridable parameters";

      // Check all parameter combos for validity
      /* FIXME
      double[] hypers = new double[_hyperSearch.length];
      // FIXME: this expect finite space!
      for( int[] hidx = new int[_hyperSearch.length]; hidx != null; hidx = nextModel(hidx) ) {
        ModelBuilder mb = getBuilder(params, hypers(hidx,hypers));
        if( mb.error_count() > 0 )
          throw new IllegalArgumentException(mb.validationErrors());
      }*/
    }

    /**
     * Compute size of grid space
     */
    protected int computeSizeOfHyperSpace() {
      int work = 1;
      for (Map.Entry<String, Object[]> p : _hyperParams.entrySet()) {
        if (p.getValue() != null) {
          work *= p.getValue().length;
        }
      }
      return work;
    }

    GridSearch start() {
      Log.info("Starting gridsearch: estimated size of search space =" + _totalModels);
      start(new H2OCountedCompleter() {
        @Override
        public void compute2() {
          gridSearch(_params);
          tryComplete();
        }
      }, _totalModels, true);
      return this;
    }

    /**
     * @return the set of models covered by this grid search, some may be null if the search is in
     * progress or otherwise incomplete. It can also contain duplicate entries if input grid search
     * specification includes them.
     */
    public Model[] models() {
      MP paramsPrototype = _params;
      Model[] ms = new Model[_totalModels];
      int mcnt = 0;
      Object[] hypers = new Object[_hyperParamNames.length];
      for (int[] hidx = new int[_hyperParamNames.length]; hidx != null; hidx = nextModel(hidx)) {
        MP params = getModelParams((MP) paramsPrototype.clone(), hypers(hidx, hypers));
        ms[mcnt++] = model(params).get();
      }
      return ms;
    }

    public int getModelsCount() {
      return _totalModels;
    }

    // Classic grid search over hyper-parameter space
    private void gridSearch(final MP paramsPrototype) {
      Object[] hypers = new Object[_hyperParamNames.length];
      for (int[] hidx = new int[_hyperParamNames.length]; hidx != null; hidx = nextModel(hidx)) {
        if (!isRunning()) {
          cancel();
          return;
        }
        final MP params = getModelParams((MP) paramsPrototype.clone(), hypers(hidx, hypers));
        buildModel(params);
      }
      // Grid search is done
      // FIXME: missing rendez-vous?
      done();
    }

    // Dumb iteration over the hyper-parameter space.
    // Return NULL at end
    private int[] nextModel(int[] hidx) {
      // Find the next parm to flip
      int i;
      for (i = 0; i < hidx.length; i++) {
        if (hidx[i] + 1 < _hyperParams.get(_hyperParamNames[i]).length) {
          break;
        }
      }
      if (i == hidx.length) {
        return null; // All done, report null
      }
      // Flip indices
      for (int j = 0; j < i; j++) {
        hidx[j] = 0;
      }
      hidx[i]++;
      return hidx;
    }

    private Object[] hypers(int[] hidx, Object[] hypers) {
      for (int i = 0; i < hidx.length; i++) {
        hypers[i] = _hyperParams.get(_hyperParamNames[i])[hidx[i]];
      }
      return hypers;
    }

    protected MP getModelParams(MP params, Object[] hyperParams) {
      ModelParametersBuilderFactory.ModelParametersBuilder<MP>
          paramsBuilder =
          _paramsBuilderFactory.get(params);
      for (int i = 0; i < _hyperParamNames.length; i++) {
        String paramName = _hyperParamNames[i];
        Object paramValue = hyperParams[i];
        paramsBuilder.set(paramName, paramValue);
      }
      return paramsBuilder.build();
    }
  }

  @Override
  protected long checksum_impl() {
    throw H2O.unimpl();
  }

  /**
   * FIXME append missing info
   * @param <MP>
   */
  static class SimpleParametersBuilderFactory<MP extends Model.Parameters>
      implements ModelParametersBuilderFactory<MP> {

    @Override
    public ModelParametersBuilder<MP> get(MP initialParams) {
      return new SimpleParamsBuilder<>(initialParams);
    }

    public static class SimpleParamsBuilder<MP extends Model.Parameters>
        implements ModelParametersBuilder<MP> {

      final private MP params;

      public SimpleParamsBuilder(MP initialParams) {
        params = initialParams;
      }

      @Override
      public ModelParametersBuilder<MP> set(String name, Object value) {
        PojoUtils.setField(params, name, value, PojoUtils.FieldNaming.CONSISTENT);
        return this;
      }

      @Override
      public MP build() {
        return params;
      }
    }
  }
}

interface HyperSpaceWalker<MP extends Model.Parameters> {

  /**
   * Get next model parameters
   *
   * @param prototype
   * @param m
   * @return
   */
  public MP nextModelParameters(MP prototype, Model m);

  /**
   * Returns hyper parameters names which are used for walking
   * the hyper parameters space.
   *
   * The names have to match the names of attributes in model parameters MP.
   *
   * @return names of used hyper parameters
   */
  public String[] getHyperParamNames();
}

package hex.grid;

import java.util.Collection;

import hex.Model;
import water.Futures;
import water.H2O;
import water.Key;
import water.Lockable;
import water.fvec.Frame;
import water.util.IcedHashMap;
import water.util.IcedLong;
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
 */
// FIXME should be abstract but REST API layer requires not abstract
// FIXME should follow model build structure with INPUT and OUTPUT
// FIXME vyzkouset grid search for Deep Learning
// FIXME failed models should be represented in grid?
public class Grid<MP extends Model.Parameters>
    extends Lockable<Grid<MP>> {

  /**
   * The training frame for this grid of models.
   */
  protected final Frame _fr;

  /**
   * A cache of double[] hyper-parameters mapping to Models.
   */
  public final IcedHashMap<IcedLong, Key<Model>> _cache = new IcedHashMap<>();

  /**
   * Used "based" model parameters for this grid search.
   */
  public final MP _params;

  /**
   * Name of model generated included in this grid.
   */
  public final String _modelName;

  /**
   * Names of used hyper parameters for this grid search.
   */
  final String[] _hyper_names;

  public Grid(Key key, MP params, String[] hyperNames, String modelName) {
    super(key);
    // FIXME really i want to save frame?
    _fr = params != null ? params.train() : null;
    _params = params != null ? (MP) params.clone() : null;
    _hyper_names = hyperNames;
    _modelName = modelName;
  }

  /**
   * @return Model name
   */
  public String modelName() {
    return _modelName;
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
   * @param params A set of hyper parameter values
   * @return A model run with these parameters, or null if the model does not exist.
   */
  public Model getModel(MP params) {
    Key<Model> mKey = getModelKey(params);
    return mKey != null ? mKey.get() : null;
  }

  public Key<Model> getModelKey(MP params) {
    long checksum = params.checksum();
    Key<Model> mKey = _cache.get(IcedLong.valueOf(checksum));
    return mKey;
  }

  /* FIXME:  should pass model parameters instead of checksum, but model
   * parameters are not imutable and model builder modifies them! */
  Key<Model> putModel(long checksum, Key<Model> modelKey) {
    return _cache.put(IcedLong.valueOf(checksum), modelKey);
  }

  public Key<Model>[] getModelKeys() {
    return _cache.values().toArray(new Key[_cache.size()]);
  }

  public Model[] getModels() {
    Collection<Key<Model>> modelKeys = _cache.values();
    Model[] models = new Model[modelKeys.size()];
    int i = 0;
    for (Key<Model> mKey : modelKeys) {
      models[i] = mKey != null ? mKey.get() : null;
      i++;
    }
    return models;
  }

  /**
   * Return value of hyper parameters used for this hyper search.
   *
   * @param parms  Model parameters
   * @return  values of hyper parameters
   */
  public Object[] getHyperValues(MP parms) {
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

  // Cleanup models and grid
  @Override
  protected Futures remove_impl(Futures fs) {
    for (Key<Model> k : _cache.values()) {
      k.remove(fs);
    }
    _cache.clear();
    return fs;
  }

  @Override
  protected long checksum_impl() {
    throw H2O.unimpl();
  }
}


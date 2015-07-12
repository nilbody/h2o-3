package hex.kmeans;

import hex.*;
import water.DKV;
import water.H2O;
import water.Key;
import water.fvec.Frame;

import java.util.Map;

/** A Grid of Models
 *  Used to explore Model hyper-parameter space.  Lazily filled in, this object
 *  represents the potentially infinite variety of hyperparameters of a given
 *  model & dataset.
 *
 *  One subclass per kind of Model, e.g. KMeans or GLM or GBM or DL.  The Grid
 *  tracks Models and their hyperparameters, and will allow discovery of
 *  existing Models by hyperparameter, or building Models on demand by
 *  hyperparameter.  The Grid can manage a (simplistic) hyperparameter search
 *  space.
 *
 *  Hyperparameter values are limited to doubles in the API, but can be
 *  anything the subclass Grid desires internally.  E.g. the Grid for KMeans
 *  will convert the initial center selection Enum to and from a simple integer
 *  value internally.
 */
public class KMeansGrid extends Grid<KMeansModel.KMeansParameters, KMeansGrid> {

  public static final String MODEL_NAME = "KMeans";

  @Override protected String modelName() { return MODEL_NAME; }

  @Override protected double suggestedNextHyperValue( int h, Model m, double[] hyperLimits ) {
    throw H2O.unimpl();
  }

  @Override
  protected ModelBuilder createBuilder(KMeansModel.KMeansParameters params) {
    return new KMeans(params);
  }

  // Factory for returning a grid based on an algorithm flavor
  private KMeansGrid( Key key, Frame fr, KMeansModel.KMeansParameters params, String[] hyperNames) {
    super(key, fr, params, hyperNames);
  }
  public static KMeansGrid get(Key<Grid> destKey, Frame fr, KMeansModel.KMeansParameters params, Map<String,Object[]> hyperParams) {
    Key k = destKey != null ? destKey : Grid.keyName(MODEL_NAME, fr);
    KMeansGrid kmg = DKV.getGet(k);
    if( kmg != null ) return kmg;
    kmg = new KMeansGrid(k, fr, params, hyperParams.keySet().toArray(new String[hyperParams.size()]));
    DKV.put(kmg);
    return kmg;
  }

  /** FIXME: Rest API requirement - do not call directly */
  public KMeansGrid() { super(null, null, null, null); }
}

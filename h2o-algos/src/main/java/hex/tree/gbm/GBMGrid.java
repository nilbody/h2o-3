package hex.tree.gbm;

import hex.*;
import hex.tree.SharedTreeGrid;
import water.DKV;
import water.util.ArrayUtils;
import water.H2O;
import water.Key;
import water.fvec.Frame;

import java.util.Map;

/** A Grid of Models
 *  Used to explore Model hyper-parameter space.  Lazily filled in, this object
 *  represents the potentially infinite variety of hyperparameters of a given
 *  model & dataset.
 *
 *  One subclass per kind of Model, e.g. GBM or GLM or GBM or DL.  The Grid
 *  tracks Models and their hyperparameters, and will allow discovery of
 *  existing Models by hyperparameter, or building Models on demand by
 *  hyperparameter.  The Grid can manage a (simplistic) hyperparameter search
 *  space.
 *
 *  Hyperparameter values are limited to doubles in the API, but can be
 *  anything the subclass Grid desires internally.  E.g. the Grid for GBM
 *  will convert the initial center selection Enum to and from a simple integer
 *  value internally.
 */
public class GBMGrid extends SharedTreeGrid<GBMModel.GBMParameters, GBMGrid> {

  public static final String MODEL_NAME = "GBM";

  @Override protected String modelName() { return MODEL_NAME; }

  @Override protected double suggestedNextHyperValue( int h, Model m, double[] hyperLimits ) {
    throw H2O.unimpl();
  }

  @Override protected GBM createBuilder(GBMModel.GBMParameters params) {
    return new GBM(params);
  }

  // Factory for returning a grid based on an algorithm flavor
  private GBMGrid( Key key, Frame fr, GBMModel.GBMParameters params, String[] hyperNames) {
    super(key, fr, params, hyperNames);
  }

  public static GBMGrid get(Key<Grid> destKey, Frame fr, GBMModel.GBMParameters params, Map<String,Object[]> hyperParams) {
    Key k = destKey != null ? destKey : Grid.keyName(MODEL_NAME, fr);
    GBMGrid kmg = DKV.getGet(k);
    if( kmg != null ) return kmg;
    kmg = new GBMGrid(k, fr, params, hyperParams.keySet().toArray(new String[hyperParams.size()]));
    DKV.put(kmg);
    return kmg;
  }
  /** FIXME: Rest API requirement - do not call directly */
  public GBMGrid() { super(null, null, null, null); }
}

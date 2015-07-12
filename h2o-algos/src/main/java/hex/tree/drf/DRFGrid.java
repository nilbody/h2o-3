package hex.tree.drf;

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
 *  One subclass per kind of Model, e.g. DRF or GLM or DRF or DL.  The Grid
 *  tracks Models and their hyperparameters, and will allow discovery of
 *  existing Models by hyperparameter, or building Models on demand by
 *  hyperparameter.  The Grid can manage a (simplistic) hyperparameter search
 *  space.
 *
 *  Hyperparameter values are limited to doubles in the API, but can be
 *  anything the subclass Grid desires internally.  E.g. the Grid for DRF
 *  will convert the initial center selection Enum to and from a simple integer
 *  value internally.
 */
public class DRFGrid extends SharedTreeGrid<DRFModel.DRFParameters, DRFGrid> {

  public static final String MODEL_NAME = "DRF";
  /** @return Model name */
  @Override protected String modelName() { return MODEL_NAME; }

  @Override protected double suggestedNextHyperValue( int h, Model m, double[] hyperLimits ) {
    throw H2O.unimpl();
  }

  @Override
  protected ModelBuilder createBuilder(DRFModel.DRFParameters params) {
    return new DRF(params);
  }

  // Factory for returning a grid based on an algorithm flavor
  private DRFGrid( Key key, Frame fr, DRFModel.DRFParameters params, String[] hyperNames) { super(key, fr, params, hyperNames); }
  public static DRFGrid get( Key<Grid> destKey, Frame fr, DRFModel.DRFParameters params, Map<String,Object[]> hyperParams) {
    Key k = destKey != null ? destKey : Grid.keyName(MODEL_NAME, fr);
    DRFGrid kmg = DKV.getGet(k);
    if( kmg != null ) return kmg;
    kmg = new DRFGrid(k, fr, params, hyperParams.keySet().toArray(new String[hyperParams.size()]));
    DKV.put(kmg);
    return kmg;
  }

  /** FIXME: Rest API requirement - do not call directly */
  public DRFGrid() { super(null, null, null, null); }
}

package hex.tree;

import hex.Grid;
import water.Key;
import water.H2O;
import water.fvec.Frame;

/** A Grid of Models
 *  Used to explore Model hyper-parameter space.  Lazily filled in, this object
 *  represents the potentially infinite variety of hyperparameters of a given
 *  model & dataset.
 */
public abstract class SharedTreeGrid<P extends SharedTreeModel.SharedTreeParameters, G extends SharedTreeGrid<P, G>> extends Grid<P, G> {

  public static final String MODEL_NAME = "SharedTree";

  @Override protected String modelName() { return MODEL_NAME; }

  protected static final String[] HYPER_NAMES    = new String[] {"ntrees", "max_depth", "min_rows", "nbins" };
  protected static final double[] HYPER_DEFAULTS = new double[] {    50   ,       5     ,     10     ,    20    };

  // Factory for returning a grid based on an algorithm flavor
  protected SharedTreeGrid( Key key, Frame fr, P params, String[] hyperNames ) { super(key, fr, params, hyperNames); }
}

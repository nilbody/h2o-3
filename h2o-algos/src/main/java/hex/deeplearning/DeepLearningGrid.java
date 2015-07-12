package hex.deeplearning;

import hex.Distribution;
import hex.Grid;
import hex.Model;
import water.DKV;
import water.H2O;
import water.Key;
import water.fvec.Frame;
import hex.deeplearning.DeepLearningParameters.*;

import java.util.Map;

/** Grid for deep learning.
 */
public class DeepLearningGrid extends Grid<DeepLearningParameters, DeepLearningGrid> {

  public static final String MODEL_NAME = "DeepLearning";

  @Override protected String modelName() { return MODEL_NAME; }

  @Override protected double suggestedNextHyperValue( int h, Model m, double[] hyperLimits ) {
    throw H2O.unimpl();
  }

  @Override protected DeepLearning createBuilder(DeepLearningParameters params) {
    return new DeepLearning(params);
  }

  // Factory for returning a grid based on an algorithm flavor
  private DeepLearningGrid(Key key, Frame fr, DeepLearningParameters params, String[] hyperNames) {
    super(key, fr, params, hyperNames);
  }

  public static DeepLearningGrid get(Key<Grid> destKey, Frame fr, DeepLearningParameters params, Map<String,Object[]> hyperParams) {
    Key k = destKey != null ? destKey : Grid.keyName(MODEL_NAME, fr);
    DeepLearningGrid kmg = DKV.getGet(k);
    if( kmg != null ) return kmg;
    kmg = new DeepLearningGrid(k, fr, params, hyperParams.keySet().toArray(new String[hyperParams.size()]));
    DKV.put(kmg);
    return kmg;
  }
  /** FIXME: Rest API requirement - do not call directly */
  public DeepLearningGrid() { super(null, null, null, null); }
}

package hex.api;

import hex.Grid;
import hex.deeplearning.DeepLearningGrid;
import hex.deeplearning.DeepLearningParameters;
import hex.schemas.DeepLearningGridSearchV99;
import hex.schemas.DeepLearningV3;
import hex.schemas.GBMGridSearchV99;
import hex.schemas.GBMV3;
import hex.tree.gbm.GBMGrid;
import hex.tree.gbm.GBMModel;
import water.Key;
import water.fvec.Frame;

import java.util.Map;

/**
 * A specific handler for GBM grid search.
 */
public class DeepLearningGridSearchHandler extends GridSearchHandler<DeepLearningGrid,
        DeepLearningGridSearchV99,
        DeepLearningParameters,
        DeepLearningV3.DeepLearningParametersV3> {

  /* This is kind of trick, since our REST framework was not able to
     recognize overloaded function do train. Hence, we use delegation here.
   */
  public DeepLearningGridSearchV99 train(int version, DeepLearningGridSearchV99 gridSearchSchema) {
    return super.do_train(version, gridSearchSchema);
  }

  @Override
  protected DeepLearningGrid createGrid(Key<Grid> destKey, Frame f, DeepLearningParameters params, Map<String,Object[]> hyperParams) {
    return DeepLearningGrid.get(destKey, f, params, hyperParams);
  }
}

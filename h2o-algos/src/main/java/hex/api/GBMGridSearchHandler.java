package hex.api;

import hex.Grid;
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
public class GBMGridSearchHandler extends GridSearchHandler<GBMGrid,
        GBMGridSearchV99,
                                                            GBMModel.GBMParameters,
                                                            GBMV3.GBMParametersV3> {

  /* This is kind of trick, since our REST framework was not able to
     recognize overloaded function do train. Hence, we use delegation here.
   */
  public GBMGridSearchV99 train(int version, GBMGridSearchV99 gridSearchSchema) {
    return super.do_train(version, gridSearchSchema);
  }

  @Override
  protected GBMGrid createGrid(Key<Grid> destKey, Frame f, GBMModel.GBMParameters params, Map<String,Object[]> hyperParams) {
    return GBMGrid.get(destKey, f, params, hyperParams);
  }
}

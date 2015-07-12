package hex.api;

import hex.Grid;
import hex.kmeans.KMeansGrid;
import hex.kmeans.KMeansModel;
import hex.schemas.KMeansGridSearchV99;
import hex.schemas.KMeansV3;
import water.Key;
import water.fvec.Frame;

import java.util.Map;

/**
 * A specific handler for GBM grid search.
 */
public class KMeansGridSearchHandler extends GridSearchHandler<KMeansGrid,
        KMeansGridSearchV99,
                                                            KMeansModel.KMeansParameters,
                                                            KMeansV3.KMeansParametersV3> {

  /* This is kind of trick, since our REST framework was not able to
     recognize overloaded function do train. Hence, we use delegation here.
   */
  public KMeansGridSearchV99 train(int version, KMeansGridSearchV99 gridSearchSchema) {
    return super.do_train(version, gridSearchSchema);
  }

  @Override
  protected KMeansGrid createGrid(Key<Grid> destKey, Frame f, KMeansModel.KMeansParameters params, Map<String,Object[]> hyperParams) {
    return KMeansGrid.get(destKey, f, params, hyperParams);
  }
}

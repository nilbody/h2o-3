package hex.schemas;

import hex.grid.Grid;
import hex.kmeans.KMeansModel;

/**
 * End-point for KMeans grid search.
 *
 * @see hex.schemas.GridSearchSchema
 */
public class KMeansGridSearchV99 extends GridSearchSchema<Grid<KMeansModel.KMeansParameters>,
    KMeansGridSearchV99,
    KMeansModel.KMeansParameters,
    KMeansV3.KMeansParametersV3> {

}

package hex.schemas;

import hex.grid.Grid;
import hex.tree.gbm.GBMModel;

/**
 * End-point for GBM grid search.
 *
 * @see hex.schemas.GridSearchSchema
 */
public class GBMGridSearchV99 extends GridSearchSchema<
    Grid<GBMModel.GBMParameters>,
    GBMGridSearchV99,
    GBMModel.GBMParameters,
    GBMV3.GBMParametersV3> {
}

package hex.schemas;

import hex.grid.Grid;
import hex.tree.drf.DRFModel;

/**
 * End-point for DRF grid search.
 *
 * @see hex.schemas.GridSearchSchema
 */
public class DRFGridSearchV99 extends GridSearchSchema<Grid<DRFModel.DRFParameters>,
    DRFGridSearchV99,
    DRFModel.DRFParameters,
    DRFV3.DRFParametersV3> {

}

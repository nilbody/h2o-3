package hex.schemas;

import hex.deeplearning.DeepLearningParameters;
import hex.grid.Grid;

/**
 * End-point for GBM grid search.
 *
 * @see GridSearchSchema
 */
public class DeepLearningGridSearchV99 extends GridSearchSchema<Grid<DeepLearningParameters>,
    DeepLearningGridSearchV99,
    DeepLearningParameters,
    DeepLearningV3.DeepLearningParametersV3> {

}

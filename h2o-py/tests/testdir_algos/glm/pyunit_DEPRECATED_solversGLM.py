from __future__ import print_function
import sys
sys.path.insert(1,"../../../")
import h2o
from tests import pyunit_utils


def glm_solvers():
    predictors = ["displacement","power","weight","acceleration","year"]

    for solver in ["AUTO", "IRLSM", "L_BFGS", "COORDINATE_DESCENT_NAIVE", "COORDINATE_DESCENT"]:
        print("Solver = {0}".format(solver))
        for family in ["binomial", "gaussian", "poisson", "tweedie", "gamma"]:
            if   family == 'binomial': response_col = "economy_20mpg"
            elif family == 'gaussian': response_col = "economy"
            else:                      response_col = "cylinders"
            print("Family = {0}".format(family))
            training_data = h2o.import_file(pyunit_utils.locate("smalldata/junit/cars_20mpg.csv"))
            if   family == 'binomial': training_data[response_col] = training_data[response_col].asfactor()
            else:                      training_data[response_col] = training_data[response_col].asnumeric()
            model = h2o.glm(x=training_data[predictors], y=training_data[response_col], family=family, alpha=[0], Lambda=[1e-5], solver=solver)
            h2o.remove(training_data)



if __name__ == "__main__":
    pyunit_utils.standalone_test(glm_solvers)
else:
    glm_solvers()

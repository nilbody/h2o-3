import sys
sys.path.insert(1,"../../")
import h2o
from tests import pyunit_utils




def varimp_test():
    
    
    train = h2o.import_file(path=pyunit_utils.locate("smalldata/iris/iris_wheader.csv"))

    # Run GBM
    my_gbm = h2o.gbm(y=train["class"], x=train[1:4], ntrees=50, learn_rate=0.1, distribution="multinomial")

    should_be_list = my_gbm.varimp()
    assert len(should_be_list) == 3, "expected varimp list to contain 3 entries, but it has " \
                                     "{0}".format(len(should_be_list))
    assert len(should_be_list[0]) == 4, "expected varimp entry to contain 4 elements (variable, relative_importance, " \
                                        "scaled_importance, percentage), but it has {0}".format(len(should_be_list[0]))



if __name__ == "__main__":
    pyunit_utils.standalone_test(varimp_test)
else:
    varimp_test()

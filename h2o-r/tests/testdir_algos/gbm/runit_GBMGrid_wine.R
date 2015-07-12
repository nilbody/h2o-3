setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source('../../h2o-runit.R')

gbm.grid.test<-
function(conn) {
    wine.hex <- h2o.uploadFile(conn, locate("smalldata/gbm_test/wine.data"), destination_frame="wine.hex")
    print(summary(wine.hex))

    hyper_params = list(ntrees = c(5, 10, 15), max_depth = c(2, 3, 4), learn_rate = c(0.1, 0.2))
    wine.grid <- h2o.grid("gbm", y = 2, x = c(1, 3:14),
                   distribution='gaussian',
                   training_frame = wine.hex, 
                   hyper_params = hyper_params)
    print(wine.grid)
    # FIXME: put here assertions on wine.grid output
    testEnd()
}

doTest("GBM Grid Test: wine.data from smalldata", gbm.grid.test)

setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source('../../h2o-runit.R')

check.deeplearning.gridlayers <- function(conn) {
  iris.hex <- h2o.uploadFile(conn, locate("smalldata/iris/iris.csv"), destination_frame="iris.hex")
  print(summary(iris.hex))

  pretty.list <- function(ll) {
    str <- lapply(ll, function(x) { paste("(", paste(x, collapse = ","), ")", sep = "") })
    paste(str, collapse = ",")
  }
  hidden_layers <- list(c(20, 20), c(50, 50, 50))
  hyper_params <- list(loss = c("MeanSquare", "CrossEntropy"), hidden = hidden_layers)
  Log.info(paste("Deep Learning grid search over hidden layers:", pretty.list(hyper_params)))
  hh <- h2o.grid("deeplearning", x=1:4, y=5, training_frame=iris.hex, hyper_params = hyper_params)
  expect_equal(length(hh@model_ids), 2)

  # Get models
  hh_models <- lapply(hh@model_ids, function(mid) { 
    model = h2o.getModel(mid)
  })
  # Check expected number of models
  expect_equal(length(hh_models), 2)

  # FIXME

  #hh_params <- lapply(hh@model, function(x) { x@model$params$hidden })
  #expect_equal(length(hh_params), length(hidden_layers))
  #expect_true(all(hh_params %in% hidden_layers))


  #cat("\n\n HH_PARAMS:")

  #print(hh_params)

  #cat("\n\n HIDDEN LAYERS:")

  #print(hidden_layers)

  #expect_true(all(hh_params %in% hidden_layers))
  #print(hh)

  testEnd()
}

doTest("Deep Learning Grid Search: Hidden Layers", check.deeplearning.gridlayers)


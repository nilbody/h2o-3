# Change this global variable to match your own system's path
ANQIS.ROOT.PATH <- "/Users/michal/Devel/projects/h2o/repos/h2o2"
ANQIS.WIN.PATH <- "C:/Users/Anqi/Documents/Work/h2o-3"
SPENCERS.ROOT.PATH <- "/Users/spencer/0xdata/h2o-dev"
ROOT.PATH <- ANQIS.ROOT.PATH
DEV.PATH  <- "/h2o-r/h2o-package/R/"
FULL.PATH <- paste(ROOT.PATH, DEV.PATH, sep="")

src <-
function() {
  warning("MAY NOT WORK ON YOUR SYSTEM -- **TRY TO CHANGE `ROOT.PATH`!**")
  to_src <- c("classes.R", "connection.R", "constants.R", "logging.R", "communication.R", 
              "kvstore.R", "exec.R", "ops.R", "frame.R", "ast.R", "astfun.R", "import.R", 
              "parse.R", "export.R", "models.R", "edicts.R", "glm.R", "glrm.R", "pca.R", "kmeans.R", 
              "gbm.R", "deeplearning.R", "naivebayes.R", "randomForest.R", "svd.R", "locate.R", "grid.R")
  require(rjson); require(RCurl)
  invisible(lapply(to_src,function(x){source(paste(FULL.PATH, x, sep = ""))}))
}
src()

conn <- h <- h2o.init("localhost", 54321, strict_version_check=F)
iris.hex <- hex <- as.h2o(iris)
hyper_parameters = list(ntrees = c(1, 5), learn_rate = c(0.1, 0.01))
g <- h2o.grid("gbm", grid_id="XXX", x=c(1:4), y = 5, training_frame=hex, hyper_params = hyper_parameters)

# ----
hidden_layers <- list(c(20, 20), c(50, 50, 50))
hyper_params = list(hidden = hidden_layers)
hyper_params = list(loss = c("MeanSquare", "CrossEntropy"))
hh <- h2o.grid("deeplearning", x=1:4, y=5, training_frame=iris.hex, loss = "CrossEntropy", hyper_params = hyper_params)

# ----
g <- h2o.grid("kmeans", x=c(1:4), y = 5)

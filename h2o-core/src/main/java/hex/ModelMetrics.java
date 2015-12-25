package hex;

import water.*;
import water.exceptions.H2OIllegalArgumentException;
import water.fvec.Frame;
import water.util.TwoDimTable;

import java.lang.reflect.Method;
import java.util.*;

/** Container to hold the metric for a model as scored on a specific frame.
 *
 *  The MetricBuilder class is used in a hot inner-loop of a Big Data pass, and
 *  when given a class-distribution, can be used to compute CM's, and AUC's "on
 *  the fly" during ModelBuilding - or after-the-fact with a Model and a new
 *  Frame to be scored.
 */
public class ModelMetrics extends Keyed<ModelMetrics> {
  public String _description;
  final Key _modelKey;
  final Key _frameKey;
  final ModelCategory _model_category;
  final long _model_checksum;
  final long _frame_checksum;
  public final long _scoring_time;

  // Cached fields - cached them when needed
  private transient Model _model;
  private transient Frame _frame;

  public final double _MSE;     // Mean Squared Error (Every model is assumed to have this, otherwise leave at NaN)

  public ModelMetrics(Model model, Frame frame, double MSE, String desc) {
    super(buildKey(model, frame));
    _description = desc;
    // Do not cache fields now
    _modelKey = model._key;
    _frameKey = frame._key;
    _model_category = model._output.getModelCategory();
    _model_checksum = model.checksum();
    _frame_checksum = frame.checksum();
    _MSE = MSE;
    _scoring_time = System.currentTimeMillis();
  }

  public long residualDegreesOfFreedom(){throw new UnsupportedOperationException("residual degrees of freedom is not supported for this metric class");}
  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Model Metrics Type: " + this.getClass().getSimpleName().substring(12) + "\n");
    sb.append(" Description: " + (_description == null ? "N/A" : _description) + "\n");
    sb.append(" model id: " + _modelKey + "\n");
    sb.append(" frame id: " + _frameKey + "\n");
    sb.append(" MSE: " + (float)_MSE + "\n");
    return sb.toString();
  }

  public Model model() { return _model==null ? (_model=DKV.getGet(_modelKey)) : _model; }
  public Frame frame() { return _frame==null ? (_frame=DKV.getGet(_frameKey)) : _frame; }

  public double mse() { return _MSE; }
  public ConfusionMatrix cm() { return null; }
  public float[] hr() { return null; }
  public AUC2 auc_obj() { return null; }
  public double auc() { AUC2 auc = auc_obj(); if (null != auc) return auc._auc; else return 0; }

  private static class MetricsComparator implements Comparator<Key<Model>> {
    String _sort_by = null;
    boolean descending = true;
    Method criterion = null;

    public MetricsComparator(String sort_by, String sort_direction) {
      this._sort_by = sort_by;
      this.descending = ! "asc".equals(sort_direction);
    }

    public int compare(Key<Model> key1, Key<Model> key2) {
      Model model1 = DKV.getGet(key1);
      Model model2 = DKV.getGet(key2);

      if (null == model1 || null == model2) throw new H2OIllegalArgumentException("Tried to compare a Model against null: " + model1 + " and " + model2);

      // TODO cross_validation_metrics
      ModelMetrics m1 = model1._output._validation_metrics;
      ModelMetrics m2 = model2._output._validation_metrics;

      // TODO: pull this check up?
      if (null == m1 ^ null == m2) throw new H2OIllegalArgumentException("Tried to compare two ModelMetrics objects, only one of which had a validation set: " + m1 + " and " + m2);

      if (null == m1 && null == m2) {
        m1 = model1._output._training_metrics;
        m2 = model2._output._training_metrics;
      }

      if (m1.getClass() != m2.getClass()) throw new H2OIllegalArgumentException("Tried to compare two ModelMetrics objects of different types: " + m1 + " and " + m2);

      if (null == criterion) {
        ConfusionMatrix cm = m1.cm();
        try {
          criterion = m1.getClass().getMethod(_sort_by);
        }
        catch (Exception e) {
          // fall through
        }

        if (null == criterion && null != cm) {
          try {
            criterion = cm.getClass().getMethod(_sort_by);
          }
          catch (Exception e) {
            // fall through
          }
        }
      }
      if (null == criterion) throw new H2OIllegalArgumentException("Failed to find ModelMetrics criterion: " + _sort_by);

      double c1, c2;
      try {
        c1 = (double) criterion.invoke(m1);
      }
      catch (Exception e) {
        throw new H2OIllegalArgumentException(
          "Failed to get metric: " + _sort_by + " from ModelMetrics object: " + m1,
          "Failed to get metric: " + _sort_by + " from ModelMetrics object: " + m1 + ", criterion: " + criterion + ", exception: " + e
          );
      }
      try {
        c2 = (double) criterion.invoke(m2);
      }
      catch (Exception e) {
        throw new H2OIllegalArgumentException(
          "Failed to get metric: " + _sort_by + " from ModelMetrics object: " + m2,
          "Failed to get metric: " + _sort_by + " from ModelMetrics object: " + m2 + ", criterion: " + criterion + ", exception: " + e
          );
      }

      if (descending) {
        return (c2 - c1 > 0 ? 1 : -1);
      } else {
        return (c1 - c2 > 0 ? 1 : -1);
      }
    }
  }

  /**
   * Return a new list of models sorted by the named criterion, such as "auc", mse", "hr", "err", "errCount",
   * "accuracy", "specificity", "recall", "precision", "mcc", "max_per_class_error", "F1", "F2", "F0point5". . .
   * @param sort_by criterion by which we should sort
   * @param modelKeys keys of models to sortm
   * @return keys of the models, sorted by the criterion
   */
  public static List<Key<Model>> sortModelsByMetric(String sort_by, String sort_order, List<Key<Model>>modelKeys) {
    List<Key<Model>> sorted = new ArrayList<>();
    sorted.addAll(modelKeys);

    Comparator<Key<Model>> c = new MetricsComparator(sort_by, sort_order);

    Collections.sort(sorted, c);
    return sorted;
  }

  public static TwoDimTable calcVarImp(VarImp vi) {
    if (vi == null) return null;
    double[] dbl_rel_imp = new double[vi._varimp.length];
    for (int i=0; i<dbl_rel_imp.length; ++i) {
      dbl_rel_imp[i] = vi._varimp[i];
    }
    return calcVarImp(dbl_rel_imp, vi._names);
  }
  public static TwoDimTable calcVarImp(final float[] rel_imp, String[] coef_names) {
    double[] dbl_rel_imp = new double[rel_imp.length];
    for (int i=0; i<dbl_rel_imp.length; ++i) {
      dbl_rel_imp[i] = rel_imp[i];
    }
    return calcVarImp(dbl_rel_imp, coef_names);
  }
  public static TwoDimTable calcVarImp(final double[] rel_imp, String[] coef_names) {
    return calcVarImp(rel_imp, coef_names, "Variable Importances", new String[]{"Relative Importance", "Scaled Importance", "Percentage"});
  }
  public static TwoDimTable calcVarImp(final double[] rel_imp, String[] coef_names, String table_header, String[] col_headers) {
    if(rel_imp == null) return null;
    if(coef_names == null) {
      coef_names = new String[rel_imp.length];
      for(int i = 0; i < coef_names.length; i++)
        coef_names[i] = "C" + String.valueOf(i+1);
    }

    // Sort in descending order by relative importance
    Integer[] sorted_idx = new Integer[rel_imp.length];
    for(int i = 0; i < sorted_idx.length; i++) sorted_idx[i] = i;
    Arrays.sort(sorted_idx, new Comparator<Integer>() {
      public int compare(Integer idx1, Integer idx2) {
        return Double.compare(-rel_imp[idx1], -rel_imp[idx2]);
      }
    });

    double total = 0;
    double max = rel_imp[sorted_idx[0]];
    String[] sorted_names = new String[rel_imp.length];
    double[][] sorted_imp = new double[rel_imp.length][3];

    // First pass to sum up relative importance measures
    int j = 0;
    for(int i : sorted_idx) {
      total += rel_imp[i];
      sorted_names[j] = coef_names[i];
      sorted_imp[j][0] = rel_imp[i];         // Relative importance
      sorted_imp[j++][1] = rel_imp[i] / max;   // Scaled importance
    }
    // Second pass to calculate percentages
    j = 0;
    for(int i : sorted_idx)
      sorted_imp[j++][2] = rel_imp[i] / total; // Percentage

    String [] col_types = new String[3];
    String [] col_formats = new String[3];
    Arrays.fill(col_types, "double");
    Arrays.fill(col_formats, "%5f");
    return new TwoDimTable(table_header, null, sorted_names, col_headers, col_types, col_formats, "Variable",
            new String[rel_imp.length][], sorted_imp);
  }

  private static Key<ModelMetrics> buildKey(Key model_key, long model_checksum, Key frame_key, long frame_checksum) {
    return Key.make("modelmetrics_" + model_key + "@" + model_checksum + "_on_" + frame_key + "@" + frame_checksum);
  }

  public static Key<ModelMetrics> buildKey(Model model, Frame frame) {
    return frame==null ? null : buildKey(model._key, model.checksum(), frame._key, frame.checksum());
  }

  public boolean isForModel(Model m) { return _model_checksum == m.checksum(); }
  public boolean isForFrame(Frame f) { return _frame_checksum == f.checksum(); }

  public static ModelMetrics getFromDKV(Model model, Frame frame) {
    Value v = DKV.get(buildKey(model, frame));
    return null == v ? null : (ModelMetrics)v.get();
  }

  @Override protected long checksum_impl() { return _frame_checksum * 13 + _model_checksum * 17; }

  /** Class used to compute AUCs, CMs & HRs "on the fly" during other passes
   *  over Big Data.  This class is intended to be embedded in other MRTask
   *  objects.  The {@code perRow} method is called once-per-scored-row, and
   *  the {@code reduce} method called once per MRTask.reduce, and the {@code
   *  <init>} called once per MRTask.map.
   */
  public static abstract class MetricBuilder<T extends MetricBuilder<T>> extends Iced {
    transient public double[] _work;
    public double _sumsqe;      // Sum-squared-error
    public long _count;
    public double _wcount;
    public double _wY; // (Weighted) sum of the response
    public double _wYY; // (Weighted) sum of the squared response

    public  double weightedSigma() {
//      double sampleCorrection = _count/(_count-1); //sample variance -> depends on the number of ACTUAL ROWS (not the weighted count)
      double sampleCorrection = 1; //this will make the result (and R^2) invariant to globally scaling the weights
      return _count <= 1 ? 0 : Math.sqrt(sampleCorrection*(_wYY/_wcount - (_wY*_wY)/(_wcount*_wcount)));
    }
    abstract public double[] perRow(double ds[], float yact[], Model m);
    public double[] perRow(double ds[], float yact[],double weight, double offset,  Model m) {
      assert(weight==1 && offset == 0);
      return perRow(ds, yact, m);
    }
    public void reduce( T mb ) {
      _sumsqe += mb._sumsqe;
      _count += mb._count;
      _wcount += mb._wcount;
      _wY += mb._wY;
      _wYY += mb._wYY;
    }

    public void postGlobal() {}

    /**
     * Having computed a MetricBuilder, this method fills in a ModelMetrics
     * @param m Model
     * @param f Scored Frame
     * @param adaptedFrame
     *@param preds Predictions of m on f (optional)  @return Filled Model Metrics object
     */
    public abstract ModelMetrics makeModelMetrics(Model m, Frame f, Frame adaptedFrame, Frame preds);
  }
}

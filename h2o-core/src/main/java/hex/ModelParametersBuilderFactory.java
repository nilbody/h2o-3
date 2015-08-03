package hex;

/**
 * Definition of factory for creating model parameters.
 */
public interface ModelParametersBuilderFactory<MP extends Model.Parameters> {

  /** Get parameters builder. */
  public ModelParametersBuilder<MP> get(MP initialParams);

  /** Reflective interface to configure initial parameters.
   *
   * The usage is sequence of <code>set</code> calls finalized by
   * <code>build</code> call which produces final version of parameters.
   *
   * @param <MP>  model parameters
   */
  public static interface ModelParametersBuilder<MP extends Model.Parameters> {

    public ModelParametersBuilder<MP> set(String name, Object value);

    public MP build();
  }
}

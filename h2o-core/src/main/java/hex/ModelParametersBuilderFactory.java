package hex;

/**
 * Definition of factory for creating model parameters.
 */
public interface ModelParametersBuilderFactory<MP extends Model.Parameters> {

  public ModelParametersBuilder<MP> get(MP initialParams);

  public static interface ModelParametersBuilder<MP extends Model.Parameters> {

    public ModelParametersBuilder<MP> set(String name, Object value);

    public MP build();
  }
}

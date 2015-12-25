package water.api;

import hex.ModelBuilder;
import hex.schemas.ModelBuilderSchema;
import water.H2O;
import water.Iced;

import java.util.Map;

class ModelBuildersHandler extends Handler {

  /** Return all the modelbuilders. */
  @SuppressWarnings("unused") // called through reflection by RequestServer
  public ModelBuildersV3 list(int version, ModelBuildersV3 m) {
    Map<String, Class<? extends ModelBuilder>> builders = ModelBuilder.getModelBuilders();
    m.model_builders = new ModelBuilderSchema.IcedHashMapStringModelBuilderSchema();

    for (Map.Entry<String, Class<? extends ModelBuilder>> entry : builders.entrySet()) {
        String algo = entry.getKey();
        ModelBuilder builder = ModelBuilder.createModelBuilder(algo);
        m.model_builders.put(algo, (ModelBuilderSchema)Schema.schema(version, builder).fillFromImpl(builder));
    }
    return m;
  }

  /** Return a single modelbuilder. */
  @SuppressWarnings("unused") // called through reflection by RequestServer
  public ModelBuildersV3 fetch(int version, ModelBuildersV3 m) {
    m.model_builders = new ModelBuilderSchema.IcedHashMapStringModelBuilderSchema();
    ModelBuilder builder = ModelBuilder.createModelBuilder(m.algo);
    m.model_builders.put(m.algo, (ModelBuilderSchema)Schema.schema(version, builder).fillFromImpl(builder));
    return m;
  }

  public static class ModelIdV3 extends Schema<Iced, ModelIdV3>{
    @API(help="Model ID", direction = API.Direction.OUTPUT)
    String model_id;
  }

  /** Calculate next unique model_id. */
  @SuppressWarnings("unused") // called through reflection by RequestServer
  public ModelIdV3 calcModelId(int version, ModelBuildersV3 m) {
    m.model_builders = new ModelBuilderSchema.IcedHashMapStringModelBuilderSchema();
    String model_id = H2O.calcNextUniqueModelId(m.algo);
    ModelIdV3 mm = new ModelIdV3();
    mm.model_id = model_id;
    return mm;
  }
}



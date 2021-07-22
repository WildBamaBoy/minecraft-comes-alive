package mca.util.compat.model;

/**
 * @since MC 1.17
 */
public class ModelPartData {
    ModelPartCompat part;

    ModelPartData(ModelPartCompat part) {
        this.part = part;
    }

    public ModelPartData getChild(String name) {
        return part.children.get(name);
    }

    public ModelPartData addChild(String name, ModelPartBuilder builder) {
        return addChild(name, builder, ModelTransform.NONE);
    }

    public ModelPartData addChild(String name, ModelPartBuilder builder, ModelTransform transform) {
        ModelPartData child = new ModelPartData(builder.part);
        part.children.put(name, child);
        part.addChild(builder.part);
        return child;
    }
}

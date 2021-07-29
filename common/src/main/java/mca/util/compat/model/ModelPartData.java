package mca.util.compat.model;

/**
 * @since MC 1.17
 */
public class ModelPartData {
    ModelPartBuilder builder;

    ModelPartData(ModelPartBuilder builder, Dilation dilation) {
        this.builder = builder;
        this.builder.part.dilation = dilation;
    }

    public ModelPartData getChild(String name) {
        return builder.children.get(name);
    }

    public ModelPartData addChild(String name, ModelPartBuilder builder) {
        return addChild(name, builder, ModelTransform.NONE);
    }

    public ModelPartData addChild(String name, ModelPartBuilder builder, ModelTransform transform) {
        ModelPartData child = new ModelPartData(builder, this.builder.part.dilation);
        child.builder.part.setTransform(transform);

        this.builder.children.put(name, child);
        this.builder.part.children.put(name, child.builder.part);
        this.builder.part.addChild(builder.part);
        return child;
    }
}

package mca.util.compat.model;

public interface BipedEntityModelCompat {
    /**
     * @since MC 1.17
     */
    static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
        ModelPartData root = new ModelPartData(ModelPartBuilder.create(), dilation);
        root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4, -8, -4, 8, 8, 8, dilation), ModelTransform.pivot(0, 0 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4, -8, -4, 8, 8, 8, dilation.add(0.5F)), ModelTransform.pivot(0, 0 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4, 0, -2, 8, 12, 4, dilation), ModelTransform.pivot(0, 0 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-3, -2, -2, 4, 12, 4, dilation), ModelTransform.pivot(-5, 2 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1, -2, -2, 4, 12, 4, dilation), ModelTransform.pivot(5, 2 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2, 4, 12, 4, dilation), ModelTransform.pivot(-1.9F, 12 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2, 0, -2, 4, 12, 4, dilation), ModelTransform.pivot(1.9F, 12 + pivotOffsetY, 0));
        return () -> root;
     }
}

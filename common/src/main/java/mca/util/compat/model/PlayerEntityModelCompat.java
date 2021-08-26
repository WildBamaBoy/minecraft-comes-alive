package mca.util.compat.model;

public interface PlayerEntityModelCompat {
    /**
     * @since MC 1.17
     */
    static ModelData getTexturedModelData(Dilation dilation, boolean slim) {
        ModelData modelData = BipedEntityModelCompat.getModelData(dilation, 0);
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("ear", ModelPartBuilder.create().uv(24, 0).cuboid(-3, -6, -1, 6, 6, 1, dilation), ModelTransform.NONE);
        modelPartData.addChild("cloak", ModelPartBuilder.create().uv(0, 0).cuboid(-5, 0, -1, 10, 16, 1, dilation, 1, 0.5F), ModelTransform.NONE);
        if (slim) {
            modelPartData.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-1, -2, -2, 3, 12, 4, dilation), ModelTransform.pivot(5, 2.5F, 0));
            modelPartData.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-2, -2, -2, 3, 12, 4, dilation), ModelTransform.pivot(-5, 2.5F, 0));
            modelPartData.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1, -2, -2, 3, 12, 4, dilation.add(0.25F)), ModelTransform.pivot(5, 2.5F, 0));
            modelPartData.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-2, -2, -2, 3, 12, 4, dilation.add(0.25F)), ModelTransform.pivot(-5, 2.5F, 0));
        } else {
            modelPartData.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-1, -2, -2, 4, 12, 4, dilation), ModelTransform.pivot(5, 2, 0));
            modelPartData.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1, -2, -2, 4, 12, 4, dilation.add(0.25F)), ModelTransform.pivot(5, 2, 0));
            modelPartData.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-3, -2, -2, 4, 12, 4, dilation.add(0.25F)), ModelTransform.pivot(-5, 2, 0));
        }

        modelPartData.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(16, 48).cuboid(-2, 0, -2, 4, 12, 4, dilation), ModelTransform.pivot(1.9F, 12, 0));
        modelPartData.addChild("left_pants", ModelPartBuilder.create().uv(0, 48).cuboid(-2, 0, -2, 4, 12, 4, dilation.add(0.25F)), ModelTransform.pivot(1.9F, 12, 0));
        modelPartData.addChild("right_pants", ModelPartBuilder.create().uv(0, 32).cuboid(-2, 0, -2, 4, 12, 4, dilation.add(0.25F)), ModelTransform.pivot(-1.9F, 12, 0));
        modelPartData.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(16, 32).cuboid(-4, 0, -2, 8, 12, 4, dilation.add(0.25F)), ModelTransform.NONE);
        return modelData;
    }
}

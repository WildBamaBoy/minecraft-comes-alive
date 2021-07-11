package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class VillagerEntityModelMCA<T extends VillagerEntityMCA> extends VillagerEntityBaseModelMCA<T> {

    public final ModelPart breastsWear;
    public final ModelPart leftArmwear;
    public final ModelPart rightArmwear;
    public final ModelPart leftLegwear;
    public final ModelPart rightLegwear;
    public final ModelPart bodyWear;

    public VillagerEntityModelMCA(ModelPart tree, boolean clothing, boolean hideWear) {
        super(tree, clothing);

        bodyWear = tree.getChild("jacket");
        leftArmwear = tree.getChild("left_sleeve");
        rightArmwear = tree.getChild("right_sleeve");
        leftLegwear = tree.getChild("left_pants");
        rightLegwear = tree.getChild("right_pants");
        breastsWear = tree.getChild("breastplate");

        if (hideWear) {
            breastsWear.visible = false;
            leftArmwear.visible = false;
            rightArmwear.visible = false;
            leftLegwear.visible = false;
            rightLegwear.visible = false;
            bodyWear.visible = false;
        }
    }

    public static ModelData getModelData(Dilation dilation, float headSize, boolean clothing) {
        ModelData modelData = VillagerEntityBaseModelMCA.getModelData(dilation, clothing);
        ModelPartData data = modelData.getRoot();

        data.addChild(EntityModelPartNames.HEAD,
                ModelPartBuilder.create().uv(32, 0).cuboid(-4, -8, -4, 8, 8, 8, dilation),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild(EntityModelPartNames.HAT,
                ModelPartBuilder.create().uv(32, 0).cuboid(-4, -8, -4, 8, 8, 8, dilation.add(headSize + 0.5F)),
                ModelTransform.pivot(0, 0, 0)
        );
        data.addChild(EntityModelPartNames.LEFT_LEG,
                ModelPartBuilder.create().uv(16, 48).cuboid(-2, 0, -2, 4, 12, 4, dilation),
                ModelTransform.pivot(1.9F, 12, 0)
        );
        data.addChild("left_pants",
                ModelPartBuilder.create().uv(0, 48).cuboid(-2, 0, -2, 4, 12, 4, dilation.add(0.25F)),
                ModelTransform.pivot(1.9F, 12, 0)
        );
        data.addChild("right_pants",
                ModelPartBuilder.create().uv(0, 32).cuboid(-2, 0, -2, 4, 12, 4, dilation.add(0.25F)),
                ModelTransform.pivot(-1.9F, 12, 0)
        );

        data.addChild(EntityModelPartNames.LEFT_ARM,
                ModelPartBuilder.create().uv(32, 48).cuboid(-1, -2, -2, 4, 12, 4, dilation),
                ModelTransform.pivot(5, 2, 0)
        );
        data.addChild("left_sleeve",
                ModelPartBuilder.create().uv(48, 48).cuboid(-1, -2, -2, 4, 12, 4, dilation.add(0.25F)),
                ModelTransform.pivot(5, 2, 0)
        );
        data.addChild("right_sleeve",
                ModelPartBuilder.create().uv(40, 32).cuboid(-3, -2, -2, 4, 12, 4, dilation.add(0.25F)),
                ModelTransform.pivot(5, 2, 0)
        );

        data.addChild("jacket",
                ModelPartBuilder.create().uv(16, 32).cuboid(-4, 0, -2, 8, 12, 4, dilation.add(0.25F)),
                ModelTransform.pivot(0, 0, 0)
        );

        VillagerEntityBaseModelMCA.newBreasts("breastplate", data, dilation.add(0.25F), clothing, 16);

        return modelData;
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg, bodyWear, leftLegwear, rightLegwear, leftArmwear, rightArmwear);
    }

    @Override
    protected Iterable<ModelPart> breastsParts() {
        return ImmutableList.of(breasts, breastsWear);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        leftLegwear.copyTransform(leftLeg);
        rightLegwear.copyTransform(rightLeg);
        leftArmwear.copyTransform(leftArm);
        rightArmwear.copyTransform(rightArm);
        bodyWear.copyTransform(body);
        breastsWear.copyTransform(body);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        leftArmwear.visible = visible;
        rightArmwear.visible = visible;
        leftLegwear.visible = visible;
        rightLegwear.visible = visible;
        bodyWear.visible = visible;
    }

    public void copyPropertiesTo(VillagerEntityModelMCA<T> target) {
        super.setAttributes(target);

        target.leftLegwear.copyTransform(leftLegwear);
        target.rightLegwear.copyTransform(rightLegwear);
        target.leftArmwear.copyTransform(leftArmwear);
        target.rightArmwear.copyTransform(rightArmwear);
        target.bodyWear.copyTransform(bodyWear);
        target.breastsWear.copyTransform(breastsWear);
    }
}
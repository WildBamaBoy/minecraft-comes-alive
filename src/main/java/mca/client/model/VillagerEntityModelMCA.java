package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.ModelPart;

public class VillagerEntityModelMCA<T extends VillagerEntityMCA> extends VillagerEntityBaseModelMCA<T> {
    public final ModelPart breasts;

    public final ModelPart breastsWear;
    public final ModelPart leftArmwear;
    public final ModelPart rightArmwear;
    public final ModelPart leftLegwear;
    public final ModelPart rightLegwear;
    public final ModelPart bodyWear;

    public VillagerEntityModelMCA(float modelSize, float headSize, boolean cloth, boolean hideWear) {
        this(modelSize, headSize, cloth);

        if (hideWear) {
            breastsWear.visible = false;
            leftArmwear.visible = false;
            rightArmwear.visible = false;
            leftLegwear.visible = false;
            rightLegwear.visible = false;
            bodyWear.visible = false;
        }
    }

    public VillagerEntityModelMCA() {
        this(1.0f, 1.0f, true);
    }

    public VillagerEntityModelMCA(float modelSize, float headSize, boolean cloth) {
        super(64, modelSize, cloth);

        //head
        head = new ModelPart(this, 0, 0);
        head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize);
        head.setPivot(0.0F, 0.0F, 0.0F);
        hat = new ModelPart(this, 32, 0);
        hat.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize + 0.5F);
        hat.setPivot(0.0F, 0.0F, 0.0F);

        //arms
        leftArm = new ModelPart(this, 32, 48);
        leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        leftArm.setPivot(5.0F, 2.0F, 0.0F);
        leftArmwear = new ModelPart(this, 48, 48);
        leftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        leftArmwear.setPivot(5.0F, 2.0F, 0.0F);
        rightArmwear = new ModelPart(this, 40, 32);
        rightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        rightArmwear.setPivot(-5.0F, 2.0F, 0.0F);

        //legs
        leftLeg = new ModelPart(this, 16, 48);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        leftLeg.setPivot(1.9F, 12.0F, 0.0F);
        leftLegwear = new ModelPart(this, 0, 48);
        leftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        leftLegwear.setPivot(1.9F, 12.0F, 0.0F);
        rightLegwear = new ModelPart(this, 0, 32);
        rightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        rightLegwear.setPivot(-1.9F, 12.0F, 0.0F);
        bodyWear = new ModelPart(this, 16, 32);
        bodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        bodyWear.setPivot(0.0F, 0.0F, 0.0F);

        breasts = newBreasts(modelSize, cloth, 0);
        breastsWear = newBreasts(modelSize + 0.25F, cloth, 16);
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

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        leftLegwear.copyTransform(leftLeg);
        rightLegwear.copyTransform(rightLeg);
        leftArmwear.copyTransform(leftArm);
        rightArmwear.copyTransform(rightArm);
        bodyWear.copyTransform(body);
        breasts.copyTransform(body);
        breastsWear.copyTransform(body);
    }

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
        target.breasts.copyTransform(breasts);
        target.breastsWear.copyTransform(breastsWear);
    }
}
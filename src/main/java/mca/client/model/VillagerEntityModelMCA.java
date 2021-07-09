package mca.client.model;

import com.google.common.collect.ImmutableList;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.model.ModelRenderer;

public class VillagerEntityModelMCA<T extends VillagerEntityMCA> extends VillagerEntityBaseModelMCA<T> {
    public final ModelRenderer breasts;

    public final ModelRenderer breastsWear;
    public final ModelRenderer leftArmwear;
    public final ModelRenderer rightArmwear;
    public final ModelRenderer leftLegwear;
    public final ModelRenderer rightLegwear;
    public final ModelRenderer bodyWear;

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
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize);
        head.setPos(0.0F, 0.0F, 0.0F);
        hat = new ModelRenderer(this, 32, 0);
        hat.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize + 0.5F);
        hat.setPos(0.0F, 0.0F, 0.0F);

        //arms
        leftArm = new ModelRenderer(this, 32, 48);
        leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArmwear = new ModelRenderer(this, 48, 48);
        leftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        leftArmwear.setPos(5.0F, 2.0F, 0.0F);
        rightArmwear = new ModelRenderer(this, 40, 32);
        rightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        rightArmwear.setPos(-5.0F, 2.0F, 0.0F);

        //legs
        leftLeg = new ModelRenderer(this, 16, 48);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLegwear = new ModelRenderer(this, 0, 48);
        leftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        leftLegwear.setPos(1.9F, 12.0F, 0.0F);
        rightLegwear = new ModelRenderer(this, 0, 32);
        rightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        rightLegwear.setPos(-1.9F, 12.0F, 0.0F);
        bodyWear = new ModelRenderer(this, 16, 32);
        bodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        bodyWear.setPos(0.0F, 0.0F, 0.0F);

        breasts = newBreasts(modelSize, cloth, 0);
        breastsWear = newBreasts(modelSize + 0.25F, cloth, 16);
    }

    @Override
    protected Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg, bodyWear, leftLegwear, rightLegwear, leftArmwear, rightArmwear);
    }

    @Override
    protected Iterable<ModelRenderer> breastsParts() {
        return ImmutableList.of(breasts, breastsWear);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        leftLegwear.copyFrom(leftLeg);
        rightLegwear.copyFrom(rightLeg);
        leftArmwear.copyFrom(leftArm);
        rightArmwear.copyFrom(rightArm);
        bodyWear.copyFrom(body);
        breasts.copyFrom(body);
        breastsWear.copyFrom(body);
    }

    public void setVisible(boolean visible) {
        super.setAllVisible(visible);

        leftArmwear.visible = visible;
        rightArmwear.visible = visible;
        leftLegwear.visible = visible;
        rightLegwear.visible = visible;
        bodyWear.visible = visible;
    }

    public void copyPropertiesTo(VillagerEntityModelMCA<T> target) {
        super.copyPropertiesTo(target);

        target.leftLegwear.copyFrom(leftLegwear);
        target.rightLegwear.copyFrom(rightLegwear);
        target.leftArmwear.copyFrom(leftArmwear);
        target.rightArmwear.copyFrom(rightArmwear);
        target.bodyWear.copyFrom(bodyWear);
        target.breasts.copyFrom(breasts);
        target.breastsWear.copyFrom(breastsWear);
    }
}
package mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerEntityModelMCA<T extends VillagerEntityMCA> extends BipedModel<T> {
    public final ModelRenderer breasts;
    public final ModelRenderer breastsWear;
    public final ModelRenderer leftArmwear;
    public final ModelRenderer rightArmwear;
    public final ModelRenderer leftLegwear;
    public final ModelRenderer rightLegwear;
    public final ModelRenderer bodyWear;
    private final ModelRenderer bipedCape;
    private final boolean cloth;
    public float breastSize;

    public VillagerEntityModelMCA() {
        this(0.0f, 0.0f, false);
    }

    public VillagerEntityModelMCA(float modelSize, float headSize, boolean cloth) {
        super(modelSize, 0.0F, 64, 64);

        bipedCape = new ModelRenderer(this, 0, 0);
        bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, modelSize);

        //head
        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.hat = new ModelRenderer(this, 32, 0);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize + 0.5F);
        this.hat.setPos(0.0F, 0.0F, 0.0F);

        //arms
        this.leftArm = new ModelRenderer(this, 32, 48);
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.leftArm.setPos(5.0F, 2.0F, 0.0F);
        this.leftArmwear = new ModelRenderer(this, 48, 48);
        this.leftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.leftArmwear.setPos(5.0F, 2.0F, 0.0F);
        this.rightArmwear = new ModelRenderer(this, 40, 32);
        this.rightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.rightArmwear.setPos(-5.0F, 2.0F, 0.0F);

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

        this.cloth = cloth;
        breasts = newBreasts(modelSize, cloth, 0);
        breastsWear = newBreasts(modelSize + 0.25F, cloth, 16);
        breastSize = 1.0f;
    }

    private ModelRenderer newBreasts(float modelSize, boolean cloth, int oy) {
        ModelRenderer breasts = new ModelRenderer(this, 18, 21 + oy);
        if (cloth) {
            breasts.addBox(-3.25F, -1.25F, -1.5F, 6, 3, 3, modelSize);
        } else {
            breasts.texOffs(17, 21 + oy);
            breasts.addBox(-3.25F, -1.25F, -1.5F, 3, 3, 3, modelSize);
            breasts.texOffs(22, 21 + oy);
            breasts.addBox(0.25F, -1.25F, -1.5F, 3, 3, 3, modelSize);
        }
        breasts.setPos(0F, 0F, 0F);
        breasts.mirror = true;
        return breasts;
    }

    @Override
    protected Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg, hat, bodyWear, leftLegwear, rightLegwear, leftArmwear, rightArmwear);
    }


    public void renderToBuffer(MatrixStack transform, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        super.renderToBuffer(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);

        //breasts
        if (breasts.visible) {
            float sc = breastSize;
            transform.pushPose();
            transform.translate(cloth ? 0.0625 * 0.25 : 0.0, 0.175D + sc * 0.1, -0.11D);
            transform.scale(cloth ? 1.166666f : 1.0f, 1.0f, 0.75f + sc * 0.5f);
            transform.scale(sc * 0.3f + 0.85f, sc * 0.75f + 0.75f, sc * 0.75f + 0.75f);
            breasts.xRot = (float) Math.PI * 0.3f; //TODO yes this will cause distortion because of wrong matrix order
            breasts.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            breastsWear.copyFrom(breasts);
            breastsWear.render(transform, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            transform.popPose();
        }
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
        bipedCape.visible = visible;
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
package mca.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class ModelVillagerMCA<T extends EntityVillagerMCA> extends BipedModel {
    public ModelRenderer bipedLeftArmwear;
    public ModelRenderer bipedRightArmwear;
    public ModelRenderer bipedLeftLegwear;
    public ModelRenderer bipedRightLegwear;
    public ModelRenderer bipedBodyWear;

    private final ModelRenderer bipedCape;

    private final ModelRenderer breasts;
    private final ModelRenderer breastsWear;

    private final boolean cloth;

    public ModelVillagerMCA() {
        this(0.0f, 0.0f, false);
    }

    public ModelVillagerMCA(float modelSize, float headSize, boolean cloth) {
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
        this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmwear.setPos(5.0F, 2.0F, 0.0F);
        this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmwear.setPos(-5.0F, 2.0F, 10.0F);

        //legs
        leftLeg = new ModelRenderer(this, 16, 48);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        bipedLeftLegwear.setPos(1.9F, 12.0F, 0.0F);
        bipedRightLegwear = new ModelRenderer(this, 0, 32);
        bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        bipedRightLegwear.setPos(-1.9F, 12.0F, 0.0F);
        bipedBodyWear = new ModelRenderer(this, 16, 32);
        bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        bipedBodyWear.setPos(0.0F, 0.0F, 0.0F);

        this.cloth = cloth;
        breasts = newBreasts(modelSize, cloth, 0);
        breastsWear = newBreasts(modelSize + 0.25F, cloth, 16);
    }

    private ModelRenderer newBreasts(float modelSize, boolean cloth, int oy) {
        ModelRenderer breasts = new ModelRenderer(this, 18, 22 + oy);
        if (cloth) {
            breasts.addBox(-3.25F, -1.5F, -1.25F, 6, 3, 3, modelSize);
        } else {
            breasts.texOffs(17, 22 + oy);
            breasts.addBox(-3.25F, -1.5F, -1.25F, 3, 3, 3, modelSize);
            breasts.texOffs(22, 22 + oy);
            breasts.addBox(0.25F, -1.5F, -1.25F, 3, 3, 3, modelSize);
        }
        breasts.setPos(0F, 0F, 0F);
        breasts.mirror = true;
        return breasts;
    }

    public void renderToBuffer(MatrixStack p_225598_1_, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        super.renderToBuffer(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);

//        GlStateManager.pushMatrix();

//        if (isChild) {
//            GlStateManager.scale(0.5F, 0.5F, 0.5F);
//            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
//        } else {
//            if (entity.isSneaking()) {
//                GlStateManager.translate(0.0F, 0.2F, 0.0F);
//            }
//        }

        bipedLeftLegwear.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        bipedRightLegwear.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        bipedLeftArmwear.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        bipedRightArmwear.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        bipedBodyWear.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);

        //renderCape(scale);

        //breasts
//        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
//        if (EnumGender.byId(villager.get(EntityVillagerMCA.GENDER)) == EnumGender.FEMALE && !villager.isChild() && villager.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == ItemStack.EMPTY) {
            double sc = 1.0f;//villager.get(EntityVillagerMCA.GENE_BREAST);
            GL11.glPushMatrix();
            GL11.glTranslated(cloth ? 0.0625 * 0.25 : 0.0, 0.175D + sc * 0.175, -0.11D);
            GL11.glScaled(cloth ? 1.166666 : 1.0, 1.0, 0.75 + sc * 0.5);
            GL11.glRotatef(60.0F, 1.0F, 0.0F, 0.0F);
            GL11.glScaled(sc * 0.3 + 0.85, sc * 0.75 + 0.75, sc * 0.75 + 0.75);
            breasts.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            breastsWear.render(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            GL11.glPopMatrix();
//        }

//        GlStateManager.popMatrix();
    }

    public void setupAnim(EntityVillagerMCA entity, float limbSwing, float limbSwingAmount, float ageInTicks, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, p_225597_5_, p_225597_6_);

        bipedLeftLegwear.copyFrom(leftLeg);
        bipedRightLegwear.copyFrom(rightLeg);
        bipedLeftArmwear.copyFrom(leftArm);
        bipedRightArmwear.copyFrom(rightArm);
        bipedBodyWear.copyFrom(body);
        breasts.copyFrom(body);
        breastsWear.copyFrom(body);

//        if (entityIn.isSneaking()) {
//            bipedCape.y = 2.0F;
//        } else {
//            bipedCape.y = 0.0F;
//        }
    }

    public void setVisible(boolean visible) {
        super.setAllVisible(visible);
        bipedLeftArmwear.visible = visible;
        bipedRightArmwear.visible = visible;
        bipedLeftLegwear.visible = visible;
        bipedRightLegwear.visible = visible;
        bipedBodyWear.visible = visible;
        bipedCape.visible = visible;
    }
}
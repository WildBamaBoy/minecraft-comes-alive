package mca.client.model;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelVillagerMCA extends ModelBiped {
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
        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedHeadwear = new ModelRenderer(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headSize + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);

        //arms
        this.bipedLeftArm = new ModelRenderer(this, 32, 48);
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);

        //legs
        bipedLeftLeg = new ModelRenderer(this, 16, 48);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedRightLegwear = new ModelRenderer(this, 0, 32);
        bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedBodyWear = new ModelRenderer(this, 16, 32);
        bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.cloth = cloth;
        breasts = newBreasts(modelSize, cloth, 0);
        breastsWear = newBreasts(modelSize + 0.25F, cloth, 16);
    }

    private ModelRenderer newBreasts(float modelSize, boolean cloth, int oy) {
        ModelRenderer breasts = new ModelRenderer(this, 18, 22 + oy);
        if (cloth) {
            breasts.addBox(-3.25F, -1.5F, -1.25F, 6, 3, 3, modelSize);
        } else {
            breasts.setTextureOffset(17, 22 + oy);
            breasts.addBox(-3.25F, -1.5F, -1.25F, 3, 3, 3, modelSize);
            breasts.setTextureOffset(22, 22 + oy);
            breasts.addBox(0.25F, -1.5F, -1.25F, 3, 3, 3, modelSize);
        }
        breasts.setRotationPoint(0F, 0F, 0F);
        breasts.mirror = true;
        return breasts;
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        GlStateManager.pushMatrix();

        if (isChild) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
        } else {
            if (entity.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
        }

        bipedLeftLegwear.render(scale);
        bipedRightLegwear.render(scale);
        bipedLeftArmwear.render(scale);
        bipedRightArmwear.render(scale);
        bipedBodyWear.render(scale);

        //renderCape(scale);

        //breasts
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        if (EnumGender.byId(villager.get(EntityVillagerMCA.GENDER)) == EnumGender.FEMALE && !villager.isChild() && villager.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == ItemStack.EMPTY) {
            double sc = villager.get(EntityVillagerMCA.GENE_BREAST);
            GL11.glPushMatrix();
            GL11.glTranslated(cloth ? 0.0625 * 0.25 : 0.0, 0.175D + sc * 0.175, -0.11D);
            GL11.glScaled(cloth ? 1.166666 : 1.0, 1.0, 0.75 + sc * 0.5);
            GL11.glRotatef(60.0F, 1.0F, 0.0F, 0.0F);
            GL11.glScaled(sc * 0.3 + 0.85, sc * 0.75 + 0.75, sc * 0.75 + 0.75);
            breasts.render(scale);
            breastsWear.render(scale);
            GL11.glPopMatrix();
        }

        GlStateManager.popMatrix();
    }

    public void renderCape(float scale) {
        bipedCape.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
        copyModelAngles(bipedRightLeg, bipedRightLegwear);
        copyModelAngles(bipedLeftArm, bipedLeftArmwear);
        copyModelAngles(bipedRightArm, bipedRightArmwear);
        copyModelAngles(bipedBody, bipedBodyWear);
        copyModelAngles(bipedBody, breasts);
        copyModelAngles(bipedBody, breastsWear);

        if (entityIn.isSneaking()) {
            bipedCape.rotationPointY = 2.0F;
        } else {
            bipedCape.rotationPointY = 0.0F;
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        bipedLeftArmwear.showModel = visible;
        bipedRightArmwear.showModel = visible;
        bipedLeftLegwear.showModel = visible;
        bipedRightLegwear.showModel = visible;
        bipedBodyWear.showModel = visible;
        bipedCape.showModel = visible;
    }

    public void postRenderArm(float scale, EnumHandSide side) {
        ModelRenderer modelrenderer = getArmForSide(side);
        modelrenderer.postRender(scale);
    }
}
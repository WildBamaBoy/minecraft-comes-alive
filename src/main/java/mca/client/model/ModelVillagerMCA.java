package mca.client.model;

import mca.enums.EnumGender;
import org.lwjgl.opengl.GL11;

import mca.entity.EntityVillagerMCA;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ModelVillagerMCA extends ModelBiped {
    private ModelRenderer breasts;

    public ModelVillagerMCA() {
        super(0.0F, 0.0F, 64, 64);
        breasts = new ModelRenderer(this, 18, 21);
        breasts.addBox(-3F, 0F, -1F, 6, 3, 3);
        breasts.setRotationPoint(0F, 3.5F, -3F);
        breasts.setTextureSize(64, 64);
        breasts.mirror = true;
    }

    @Override
    public void render(Entity entity, float swing, float swingAmount, float age, float headYaw, float headPitch, float scale) {
        super.render(entity, swing, swingAmount, age, headYaw, headPitch, scale);
        EntityVillagerMCA villager = (EntityVillagerMCA)entity;
        if (EnumGender.byId(villager.get(EntityVillagerMCA.GENDER)) == EnumGender.FEMALE && !villager.isChild() && villager.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == ItemStack.EMPTY) {
            GL11.glPushMatrix();
            GL11.glTranslated(0.005D, -0.05D, -0.28D);
            GL11.glScaled(1.15D, 1.0D, 1.0D);
            GL11.glRotatef(60.0F, 1.0F, 0.0F, 0.0F);
            breasts.render(scale);
            GL11.glPopMatrix();
        }
    }
}
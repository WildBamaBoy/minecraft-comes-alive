package mca.client.render;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumAgeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

public class RenderVillagerMCA<T extends EntityVillagerMCA> extends RenderBiped<EntityVillagerMCA> {
    public RenderVillagerMCA(RenderManager manager) {
        super(manager, new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
    }

    @Override
    protected void preRenderCallback(EntityVillagerMCA villager, float partialTickTime) {
        if (villager.isChild()) {
            float scaleForAge = EnumAgeState.byId(villager.get(EntityVillagerMCA.AGE_STATE)).getScaleForAge();
            GlStateManager.scale(scaleForAge, scaleForAge, scaleForAge);
        }

        if (villager.isRiding()) {
            GlStateManager.translate(0, 0.5, 0);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVillagerMCA villager) {
        return villager.getTextureResourceLocation();
    }

    @Override
    protected boolean canRenderName(EntityVillagerMCA entity) {
        float distance = Minecraft.getMinecraft().player.getDistance(entity);
        return distance < 5F;
    }
}

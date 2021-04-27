package mca.client.render;

import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@OnlyIn(Dist.CLIENT)
public class LayerClothing extends LayerVillager {
    public LayerClothing(LivingRenderer<?> rendererIn) {
        super(rendererIn, 0.16666f, 0.0833f);
    }

    @Override
    String getTexture(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.get(EntityVillagerMCA.clothes);
    }
}

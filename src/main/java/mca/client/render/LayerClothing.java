package mca.client.render;

import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerClothing extends LayerVillager {
    public LayerClothing(IEntityRenderer renderer, BipedModel model) {
        super(renderer, model);
    }

    @Override
    String getTexture(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.clothes.get();
    }
}
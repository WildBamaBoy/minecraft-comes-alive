package mca.client.render;

import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerClothing extends LayerVillager<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    public LayerClothing(IEntityRenderer renderer, ModelVillagerMCA<EntityVillagerMCA> model) {
        super(renderer, model);
    }

    @Override
    String getTexture(EntityVillagerMCA villager) {
        return villager.clothes.get();
    }
}
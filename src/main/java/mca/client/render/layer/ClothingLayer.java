package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

public class ClothingLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public ClothingLayer(FeatureRendererContext<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);
    }

    @Override
    String getTexture(VillagerEntityMCA villager) {
        return villager.clothes.get();
    }
}
package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerLike;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.mob.MobEntity;

public class ClothingLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {
    public ClothingLayer(FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer, VillagerEntityModelMCA<T> model) {
        super(renderer, model);
    }

    @Override
    protected String getSkin(T villager) {
        return villager.getClothes();
    }
}
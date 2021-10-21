package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerLike;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class ClothingLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {

    private final String variant;

    public ClothingLayer(FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer, VillagerEntityModelMCA<T> model, String variant) {
        super(renderer, model);
        this.variant = variant;
    }

    @Override
    protected Identifier getSkin(T villager) {
        return cached(villager.getClothes() + variant, clothes -> {
            Identifier id = new Identifier(villager.getClothes());

            String path = id.getPath();

            // use it if it's valid
            if (canUse(id)) {
                return id;
            }

            // old values require conversion
            if (path.startsWith("skins")) {
                Identifier converted = new Identifier(id.getNamespace(), path.replace("/clothing/", "/clothing/" + variant));
                if (canUse(converted)) {
                    return converted;
                }
            }

            return new Identifier(id.getNamespace(), String.format("skins/clothing/%s/%s", variant, path));
        });
    }
}

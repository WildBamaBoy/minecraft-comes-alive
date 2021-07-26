package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.HairColors;
import mca.entity.VillagerLike;
import mca.entity.ai.Genetics;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.DyeColor;

import java.util.Optional;

public class HairLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {
    public HairLayer(FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer, VillagerEntityModelMCA<T> model) {
        super(renderer, model);

        this.model.leftLeg.visible = false;
        this.model.leftLegwear.visible = false;
        this.model.rightLeg.visible = false;
        this.model.rightLegwear.visible = false;
    }

    @Override
    protected String getSkin(T villager) {
        return villager.getHair().texture();
    }

    @Override
    protected String getOverlay(T villager) {
        return villager.getHair().overlay();
    }

    @Override
    protected float[] getColor(T villager) {
        Optional<DyeColor> hairDye = villager.getHairDye();
        if (hairDye.isPresent()) {
            return hairDye.get().getColorComponents();
        }

        return HairColors.PALLET.getColor(
                villager.getGenetics().getGene(Genetics.EUMELANIN),
                villager.getGenetics().getGene(Genetics.PHEOMELANIN),
                0
        );
    }
}

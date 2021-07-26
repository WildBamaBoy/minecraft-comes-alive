package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.HairColors;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Genetics;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.DyeColor;

import java.util.Optional;

public class HairLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public HairLayer(FeatureRendererContext<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);

        this.model.leftLeg.visible = false;
        this.model.leftLegwear.visible = false;
        this.model.rightLeg.visible = false;
        this.model.rightLegwear.visible = false;
    }

    @Override
    protected String getSkin(VillagerEntityMCA villager) {
        return villager.getHair().texture();
    }

    @Override
    protected String getOverlay(VillagerEntityMCA villager) {
        return villager.getHair().overlay();
    }

    @Override
    protected float[] getColor(VillagerEntityMCA villager) {
        Optional<DyeColor> hairDye = villager.getHairDye();
        if (hairDye.isPresent()) {
            DyeColor dyeColor = hairDye.get();
            float[] color = dyeColor.getColorComponents();
            return new float[]{color[0], color[1], color[2]};
        } else {
            float e = villager.getGenetics().getGene(Genetics.EUMELANIN);
            float p = villager.getGenetics().getGene(Genetics.PHEOMELANIN);
            double[] color = HairColors.getColor(e, p);
            return new float[]{(float) color[0], (float) color[1], (float) color[2]};
        }
    }
}

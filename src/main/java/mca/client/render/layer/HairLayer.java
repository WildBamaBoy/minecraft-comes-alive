package mca.client.render.layer;

import mca.client.colors.HairColors;
import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

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
        return villager.hair.get();
    }

    @Override
    protected String getOverlay(VillagerEntityMCA villager) {
        return villager.hairOverlay.get();
    }

    @Override
    protected float[] getColor(VillagerEntityMCA villager) {
        float e = villager.getGenetics().eumelanin.get();
        float p = villager.getGenetics().pheomelanin.get();
        double[] color = HairColors.getColor(e, p);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

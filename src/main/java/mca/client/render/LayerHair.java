package mca.client.render;

import mca.client.colors.HairColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerHair extends LayerVillager<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    public LayerHair(IEntityRenderer renderer, ModelVillagerMCA<EntityVillagerMCA> model) {
        super(renderer, model);

        // LayerViller is only designed for ModelVillagerMCA anyways
        this.model.leftLeg.visible = false;
        this.model.bipedLeftLegwear.visible = false;
        this.model.rightLeg.visible = false;
        this.model.bipedRightLegwear.visible = false;
    }

    @Override
    String getTexture(EntityVillagerMCA villager) {
        return villager.hair.get();
    }

    @Override
    String getOverlayTexture(EntityVillagerMCA villager) {
        return villager.hairOverlay.get();
    }

    @Override
    float[] getColor(EntityVillagerMCA villager) {
        float e = villager.GENE_EUMELANIN.get();
        float p = villager.GENE_PHEOMELANIN.get();
        double[] color = HairColors.getColor(e, p);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

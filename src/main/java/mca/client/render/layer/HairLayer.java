package mca.client.render.layer;

import mca.client.colors.HairColors;
import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HairLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public HairLayer(IEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);

        // LayerViller is only designed for ModelVillagerMCA anyways
        this.model.leftLeg.visible = false;
        this.model.leftLegwear.visible = false;
        this.model.rightLeg.visible = false;
        this.model.rightLegwear.visible = false;
    }

    @Override
    String getTexture(VillagerEntityMCA villager) {
        return villager.hair.get();
    }

    @Override
    String getOverlayTexture(VillagerEntityMCA villager) {
        return villager.hairOverlay.get();
    }

    @Override
    float[] getColor(VillagerEntityMCA villager) {
        float e = villager.gene_eumelanin.get();
        float p = villager.gene_pheomelanin.get();
        double[] color = HairColors.getColor(e, p);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

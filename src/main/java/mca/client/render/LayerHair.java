package mca.client.render;

import mca.client.colors.HairColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerHair extends LayerVillager {
    public LayerHair(IEntityRenderer renderer, BipedModel model) {
        super(renderer, model);

        // LayerViller is only designed for ModelVillagerMCA anyways
        ((ModelVillagerMCA) this.model).leftLeg.visible = false;
        ((ModelVillagerMCA) this.model).bipedLeftLegwear.visible = false;
        ((ModelVillagerMCA) this.model).rightLeg.visible = false;
        ((ModelVillagerMCA) this.model).bipedRightLegwear.visible = false;
    }

    @Override
    String getTexture(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.hair.get();
    }

    @Override
    String getOverlayTexture(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.hairOverlay.get();
    }

    @Override
    float[] getColor(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        float e = villager.GENE_EUMELANIN.get();
        float p = villager.GENE_PHEOMELANIN.get();
        double[] color = HairColors.getColor(e, p);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

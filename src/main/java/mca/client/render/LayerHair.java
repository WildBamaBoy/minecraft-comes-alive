package mca.client.render;

import mca.client.colors.HairColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@OnlyIn(Dist.CLIENT)
public class LayerHair extends LayerVillager {
    public LayerHair(RenderLivingBase<?> rendererIn) {
        super(rendererIn, 0.0833f, 0.16666f);

        // LayerViller is only designed for ModelVillagerMCA anyways
        ((ModelVillagerMCA) this.model).bipedLeftLeg.showModel = false;
        ((ModelVillagerMCA) this.model).bipedLeftLegwear.showModel = false;
        ((ModelVillagerMCA) this.model).bipedRightLeg.showModel = false;
        ((ModelVillagerMCA) this.model).bipedRightLegwear.showModel = false;
    }

    @Override
    String getTexture(EntityLivingBase entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.get(EntityVillagerMCA.hair);
    }

    @Override
    String getOverlayTexture(EntityLivingBase entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.get(EntityVillagerMCA.hairOverlay);
    }

    @Override
    float[] getColor(EntityLivingBase entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        float e = villager.get(EntityVillagerMCA.GENE_EUMELANIN);
        float p = villager.get(EntityVillagerMCA.GENE_PHEOMELANIN);
        double[] color = HairColors.getColor(e, p);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

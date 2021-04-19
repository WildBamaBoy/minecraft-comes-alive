package mca.client.render;

import mca.client.model.ModelVillagerMCA;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerFace extends LayerVillager {
    public LayerFace(RenderLivingBase<?> rendererIn) {
        super(rendererIn, 0.0f);

        // LayerViller is only designed for ModelVillagerMCA anyways
        ((ModelVillagerMCA) this.model).setVisible(false);
        ((ModelVillagerMCA) this.model).bipedHead.showModel = true;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
    }

    @Override
    boolean hasOverlay(EntityLivingBase entity) {
        //TODO
        return false;
    }

    @Override
    String getClothing(EntityLivingBase entity) {
        //TODO
        return "mca:skins/test/face";
    }
}

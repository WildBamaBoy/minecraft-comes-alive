package mca.client.render;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerClothing extends LayerVillager {
    public LayerClothing(RenderLivingBase<?> rendererIn) {
        super(rendererIn, 0.16666f);
    }

    @Override
    boolean hasOverlay(EntityLivingBase entity) {
        return false; //clothing uses no color anyways
    }

    @Override
    String getClothing(EntityLivingBase entity) {
        //TODO
        return "mca:skins/test/clothing";
    }
}

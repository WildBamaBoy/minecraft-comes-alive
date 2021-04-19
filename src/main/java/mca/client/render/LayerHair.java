package mca.client.render;

import mca.client.colors.HairColors;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerHair extends LayerVillager {
    public LayerHair(RenderLivingBase<?> rendererIn) {
        super(rendererIn, 0.0833f);
    }

    @Override
    boolean hasOverlay(EntityLivingBase entity) {
        //TODO
        return false;
    }

    @Override
    String getClothing(EntityLivingBase entity) {
        //TODO
        return "mca:skins/test/hair";
    }

    @Override
    float[] getColor(EntityLivingBase entity) {
        double[] color = HairColors.getColor(0.5, 0.5);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

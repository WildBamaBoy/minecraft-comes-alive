package mca.client.render;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerClothing extends LayerVillager {
    public LayerClothing(RenderLivingBase<?> rendererIn) {
        super(rendererIn, 0.16666f, 0.0833f);
    }

    @Override
    String getTexture(EntityLivingBase entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        return villager.get(EntityVillagerMCA.CLOTHES);
    }
}

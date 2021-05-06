package mca.client.render;

import cobalt.util.ResourceLocationCache;
import mca.client.colors.HairColors;
import mca.client.colors.SkinColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerSkin extends LayerVillager {
    public LayerSkin(IEntityRenderer renderer, BipedModel model) {
        super(renderer, model);
    }

    @Override
    String getTexture(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        EnumGender gender = EnumGender.byId(villager.gender.get());
        int skin = (int) Math.min(9, Math.max(0, villager.GENE_SKIN.get() * 10));
        return String.format("mca:skins/skin/%s/%d.png", gender == EnumGender.FEMALE ? "female" : "male", skin);
    }

    @Override
    float[] getColor(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        float melanin = villager.GENE_MELANIN.get();
        float hemoglobin = villager.GENE_HEMOGLOBIN.get();
        double[] color = SkinColors.getColor(melanin, hemoglobin);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

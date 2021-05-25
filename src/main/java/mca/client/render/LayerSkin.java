package mca.client.render;

import mca.client.colors.SkinColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerSkin extends LayerVillager<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    public LayerSkin(IEntityRenderer renderer, ModelVillagerMCA<EntityVillagerMCA> model) {
        super(renderer, model);
    }

    @Override
    String getTexture(EntityVillagerMCA villager) {
        EnumGender gender = EnumGender.byId(villager.gender.get());
        int skin = (int) Math.min(9, Math.max(0, villager.gene_skin.get() * 10));
        return String.format("mca:skins/skin/%s/%d.png", gender == EnumGender.FEMALE ? "female" : "male", skin);
    }

    @Override
    float[] getColor(EntityVillagerMCA villager) {
        float melanin = villager.gene_melanin.get();
        float hemoglobin = villager.gene_hemoglobin.get();
        double[] color = SkinColors.getColor(melanin, hemoglobin);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

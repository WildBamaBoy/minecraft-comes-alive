package mca.client.render.layer;

import mca.client.colors.SkinColors;
import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import mca.enums.Gender;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public SkinLayer(IEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);
    }

    @Override
    String getTexture(VillagerEntityMCA villager) {
        Gender gender = Gender.byId(villager.gender.get());
        int skin = (int) Math.min(4, Math.max(0, villager.gene_skin.get() * 5));
        return String.format("mca:skins/skin/%s/%d.png", gender == Gender.FEMALE ? "female" : "male", skin);
    }

    @Override
    float[] getColor(VillagerEntityMCA villager) {
        float melanin = villager.gene_melanin.get();
        float hemoglobin = villager.gene_hemoglobin.get();
        double[] color = SkinColors.getColor(melanin, hemoglobin);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

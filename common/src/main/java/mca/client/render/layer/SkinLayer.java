package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.SkinColors;
import mca.entity.VillagerLike;
import mca.entity.ai.Genetics;
import mca.entity.ai.relationship.Gender;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.mob.MobEntity;

public class SkinLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {
    public SkinLayer(FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer, VillagerEntityModelMCA<T> model) {
        super(renderer, model);
    }

    @Override
    protected String getSkin(T villager) {
        Gender gender = villager.getGenetics().getGender();
        int skin = (int) Math.min(4, Math.max(0, villager.getGenetics().getGene(Genetics.SKIN) * 5));
        return String.format("mca:skins/skin/%s/%d.png", gender == Gender.FEMALE ? "female" : "male", skin);
    }

    @Override
    protected float[] getColor(T villager) {
        float melanin = villager.getGenetics().getGene(Genetics.MELANIN);
        float hemoglobin = villager.getGenetics().getGene(Genetics.HEMOGLOBIN);
        double[] color = SkinColors.getColor(melanin, hemoglobin);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}

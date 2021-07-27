package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.SkinColors;
import mca.entity.VillagerLike;
import mca.entity.ai.Genetics;
import mca.entity.ai.relationship.Gender;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class SkinLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {
    public SkinLayer(FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer, VillagerEntityModelMCA<T> model) {
        super(renderer, model);
    }

    @Override
    protected Identifier getSkin(T villager) {
        Gender gender = villager.getGenetics().getGender();
        int skin = (int) Math.min(4, Math.max(0, villager.getGenetics().getGene(Genetics.SKIN) * 5));
        return cached(String.format("mca:skins/skin/%s/%d.png", gender == Gender.FEMALE ? "female" : "male", skin), Identifier::new);
    }

    @Override
    protected float[] getColor(T villager) {
        return SkinColors.PALLET.getColor(
                villager.getGenetics().getGene(Genetics.MELANIN),
                villager.getGenetics().getGene(Genetics.HEMOGLOBIN),
                villager.getInfectionProgress()
        );
    }
}

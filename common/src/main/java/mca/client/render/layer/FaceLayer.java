package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.Genetics;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class FaceLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {

    private final String variant;

    public FaceLayer(
            FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer,
            VillagerEntityModelMCA<T> model, String variant) {
        super(renderer, model);
        this.variant = variant;

        model.setVisible(false);
        model.head.visible = true;
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    protected Identifier getSkin(T villager) {
        Identifier type = EntityType.getId(villager.getType());
        int totalFaces = 11;
        int index = (int) Math.min(totalFaces - 1, Math.max(0, villager.getGenetics().getGene(Genetics.FACE) * totalFaces));
        int time = villager.age / 2 + (int) (villager.getGenetics().getGene(Genetics.HEMOGLOBIN) * 65536);
        boolean blink = time % 50 == 1 || time % 57 == 1 || villager.isSleeping() || villager.isDead();

        return cached(String.format("%s:skins/face/%s/%s/%d%s.png",
                type.getNamespace(),
                variant,
                villager.getGenetics().getGender().getStrName(),
                index,
                blink ? "_blink" : ""
        ), Identifier::new);
    }
}

package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.Genetics;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class FaceLayer<T extends MobEntity & VillagerLike<T>> extends VillagerLayer<T, VillagerEntityModelMCA<T>> {
    public FaceLayer(
            FeatureRendererContext<T, VillagerEntityModelMCA<T>> renderer,
            VillagerEntityModelMCA<T> model) {
        super(renderer, model);

        this.model.setVisible(false);
        this.model.head.visible = true;
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    protected String getSkin(T villager) {
        Identifier type = EntityType.getId(villager.getType());
        int totalFaces = 11;
        int variant = (int) Math.min(totalFaces - 1, Math.max(0, villager.getGenetics().getGene(Genetics.SKIN) * totalFaces));
        int time = villager.age / 2 + (int) (villager.getGenetics().getGene(Genetics.HEMOGLOBIN) * 65536);
        boolean blink = time % 50 == 0 || time % 57 == 0 || villager.isSleeping() || villager.isDead();

        return String.format("%s:textures/entity/%s/face/%d%s.png",
                type.getNamespace(),
                type.getPath(),
                variant,
                blink ? "_blink" : ""
        );
    }
}

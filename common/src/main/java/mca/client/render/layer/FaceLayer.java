package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Genetics;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

public class FaceLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public FaceLayer(FeatureRendererContext<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);

        this.model.setVisible(false);
        this.model.head.visible = true;
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    protected String getSkin(VillagerEntityMCA villager) {
        int totalFaces = 11;
        int skin = (int) Math.min(totalFaces - 1, Math.max(0, villager.getGenetics().getGene(Genetics.SKIN, 0) * totalFaces));
        int time = villager.age / 2 + (int) (villager.getGenetics().getGene(Genetics.HEMOGLOBIN, 0) * 65536);
        boolean blink = time % 50 == 0 || time % 57 == 0 || villager.isSleeping() || villager.isDead();
        return String.format("mca:skins/faces/%s/%d%s.png", villager.getGenetics().getGender().binary().getStrName(), skin, blink ? "_blink" : "");
    }
}

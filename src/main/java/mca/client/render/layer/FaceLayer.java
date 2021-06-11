package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import mca.enums.Gender;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FaceLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public FaceLayer(IEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);

        // LayerViller is only designed for ModelVillagerMCA anyways
        this.model.setVisible(false);
        this.model.head.visible = true;
    }

    @Override
    boolean isTranslucent() {
        return true;
    }

    @Override
    String getTexture(VillagerEntityMCA villager) {
        Gender gender = Gender.byId(villager.gender.get());
        int totalFaces = 11;
        int skin = (int) Math.min(totalFaces - 1, Math.max(0, villager.gene_skin.get() * totalFaces));
        int time = villager.tickCount / 2 + (int) (villager.gene_hemoglobin.get() * 65536);
        boolean blink = time % 50 == 0 || time % 57 == 0 || villager.isSleeping() || villager.isDeadOrDying();
        return String.format("mca:skins/faces/%s/%d%s.png", gender == Gender.FEMALE ? "female" : "male", skin, blink ? "_blink" : "");
    }
}

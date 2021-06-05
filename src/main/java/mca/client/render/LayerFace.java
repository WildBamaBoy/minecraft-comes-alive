package mca.client.render;

import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerFace extends LayerVillager<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    public LayerFace(IEntityRenderer renderer, ModelVillagerMCA<EntityVillagerMCA> model) {
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
    String getTexture(EntityVillagerMCA villager) {
        EnumGender gender = EnumGender.byId(villager.gender.get());
        int totalFaces = 11;
        int skin = (int) Math.min(totalFaces - 1, Math.max(0, villager.gene_skin.get() * totalFaces));
        int time = villager.tickCount / 2 + (int) (villager.gene_hemoglobin.get() * 65536);
        boolean blink = time % 50 == 0 || time % 57 == 0 || villager.isSleeping() || villager.isDeadOrDying();
        return String.format("mca:skins/faces/%s/%d%s.png", gender == EnumGender.FEMALE ? "female" : "male", skin, blink ? "_blink" : "");
    }
}

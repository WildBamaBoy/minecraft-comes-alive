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
        int skin = (int) Math.min(1, Math.max(0, villager.gene_skin.get() * 2));
        return String.format("mca:skins/faces/%s/%d.png", gender == EnumGender.FEMALE ? "female" : "male", skin);
    }
}

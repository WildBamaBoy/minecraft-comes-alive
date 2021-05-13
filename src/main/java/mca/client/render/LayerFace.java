package mca.client.render;

import mca.client.colors.HairColors;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerFace extends LayerVillager {
    public LayerFace(IEntityRenderer renderer, BipedModel model) {
        super(renderer, model);

        // LayerViller is only designed for ModelVillagerMCA anyways
        ((ModelVillagerMCA) this.model).setVisible(false);
        ((ModelVillagerMCA) this.model).head.visible = true;
    }

    @Override
    boolean isTranslucent() {
        return true;
    }

    @Override
    String getTexture(LivingEntity entity) {
        EntityVillagerMCA villager = (EntityVillagerMCA) entity;
        EnumGender gender = EnumGender.byId(villager.gender.get());
        int skin = (int) Math.min(1, Math.max(0, villager.GENE_SKIN.get() * 2));
        return String.format("mca:skins/faces/%s/%d.png", gender == EnumGender.FEMALE ? "female" : "male", skin);
    }
}

package mca.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.client.model.ModelVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.inventory.EquipmentSlotType;

import javax.annotation.Nullable;

public class RenderVillagerMCA extends BipedRenderer<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    public RenderVillagerMCA(EntityRendererManager manager) {
        super(manager, new ModelVillagerMCA<>(), 0.5F);

        this.addLayer(new LayerSkin(this, new ModelVillagerMCA<>(0.0f, 0.0f, false)));
        this.addLayer(new LayerClothing(this, new ModelVillagerMCA<>(0.16666f, 0.0833f, true)));
        this.addLayer(new LayerHair(this, new ModelVillagerMCA<>(0.0833f, 0.16666f, false)));
        this.addLayer(new LayerFace(this, new ModelVillagerMCA<>(0.01f, 0.01f, false)));
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
    }

    @Override
    protected void scale(EntityVillagerMCA villager, MatrixStack matrixStackIn, float partialTickTime) {
        if (villager.isBaby()) {
            float scaleForAge = villager.getAgeState().getScaleForAge();
            matrixStackIn.scale(scaleForAge, scaleForAge, scaleForAge);
        }

        //dimensions
        float height = villager.gene_size.get() * 0.5f + 0.75f;
        float width = villager.gene_width.get() * 0.5f + 0.75f;
        matrixStackIn.scale(width, height, width);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(EntityVillagerMCA p_230496_1_, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
        //setting the type to null prevents it from rendering
        //we need a skin layer anyways because of the color
        return null;
    }

    @Override
    public void render(EntityVillagerMCA villager, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
        this.model.breasts.visible = EnumGender.byId(villager.gender.get()) == EnumGender.FEMALE && !villager.isBaby() && villager.getItemBySlot(EquipmentSlotType.CHEST).isEmpty();
        this.model.breastSize = villager.gene_breast.get();

        //also apply this to the layers
        for (LayerRenderer<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> l : layers) {
            if (l instanceof LayerVillager) {
                LayerVillager<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> lv = (LayerVillager<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>>) l;

                lv.model.breasts.visible = this.model.breasts.visible;
                lv.model.breastSize = this.model.breastSize;
            }
        }

        super.render(villager, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    @Override
    protected boolean shouldShowName(EntityVillagerMCA villager) {
        if (Minecraft.getInstance().player != null) {
            return Minecraft.getInstance().player.distanceToSqr(villager) < 25.0F;
        }
        return false;
    }
}

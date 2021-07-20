package mca.client.render;

import mca.client.model.VillagerEntityBaseModelMCA;
import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.Nullable;

public class VillagerEntityMCARenderer extends BipedEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public VillagerEntityMCARenderer(EntityRendererFactory.Context ctx) {
        super(ctx, createModel(0, 0, false, false), 0.5F);

        this.addFeature(new SkinLayer(this, createModel(0.0f, 0.0f, false, true)));
        this.addFeature(new ClothingLayer(this, createModel(0.0833f, 0.16666f, true, false)));
        this.addFeature(new HairLayer(this, createModel(0.16666f, 0.0833f, false, false)));
        this.addFeature(new FaceLayer(this, createModel(0.01f, 0.01f, false, true)));
        this.addFeature(new ArmorFeatureRenderer<>(this, createArmorModel(0.5f), createArmorModel(1.0f)));
        this.addFeature(new HeldItemFeatureRenderer<>(this));
    }

    private static VillagerEntityModelMCA<VillagerEntityMCA> createModel(float dilation, float headSize, boolean cloth, boolean hideWear) {
        return new VillagerEntityModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityModelMCA.getModelData(new Dilation(dilation), cloth), 64, 64)
                .createModel(), cloth, hideWear);
    }

    private static VillagerEntityBaseModelMCA<VillagerEntityMCA> createArmorModel(float modelSize) {
        return new VillagerEntityBaseModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityBaseModelMCA.getModelData(new Dilation(modelSize), true), 64, 64)
                .createModel(), true);
    }

    @Override
    protected void scale(VillagerEntityMCA villager, MatrixStack matrices, float tickDelta) {
        float height = villager.getScaleFactor();
        float width = villager.getHorizontalScaleFactor();
        matrices.scale(width, height, width);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(VillagerEntityMCA entity, boolean showBody, boolean translucent, boolean showOutlines) {
        //setting the type to null prevents it from rendering
        //we need a skin layer anyways because of the color
        return null;
    }

    @Override
    protected boolean hasLabel(VillagerEntityMCA villager) {
        return MinecraftClient.getInstance().player != null
                && MinecraftClient.getInstance().player.squaredDistanceTo(villager) < 25;
    }
}

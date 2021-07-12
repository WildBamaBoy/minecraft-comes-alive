package mca.client.render;

import mca.client.model.VillagerEntityBaseModelMCA;
import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.layer.ClothingLayer;
import mca.client.render.layer.FaceLayer;
import mca.client.render.layer.HairLayer;
import mca.client.render.layer.SkinLayer;
import mca.entity.VillagerEntityMCA;
import mca.enums.AgeState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class VillagerEntityMCARenderer extends BipedEntityRenderer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    List<VillagerEntityBaseModelMCA<VillagerEntityMCA>> models = new LinkedList<>();

    public VillagerEntityMCARenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VillagerEntityModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityModelMCA.getModelData(new Dilation(1), 1, true), 64, 64)
                .createModel(), true, false), 0.5F);

        this.addFeature(new SkinLayer(this, createModel(0.0f, 0.0f, false, true)));
        this.addFeature(new ClothingLayer(this, createModel(0.0833f, 0.16666f, true, false)));
        this.addFeature(new HairLayer(this, createModel(0.16666f, 0.0833f, false, false)));
        this.addFeature(new FaceLayer(this, createModel(0.01f, 0.01f, false, true)));
        this.addFeature(new ArmorFeatureRenderer<>(this,
                createArmorModel(0.5f),
                createArmorModel(1.0f)));
        this.addFeature(new HeldItemFeatureRenderer<>(this));
    }

    private VillagerEntityModelMCA<VillagerEntityMCA> createModel(float modelSize, float headSize, boolean cloth, boolean hideWear) {
        VillagerEntityModelMCA<VillagerEntityMCA> m = new VillagerEntityModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityModelMCA.getModelData(new Dilation(modelSize), headSize, cloth), 64, 64)
                .createModel(), cloth, hideWear);
        models.add(m);
        return m;
    }

    private VillagerEntityBaseModelMCA<VillagerEntityMCA> createArmorModel(float modelSize) {
        VillagerEntityBaseModelMCA<VillagerEntityMCA> m = new VillagerEntityBaseModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityBaseModelMCA.getModelData(new Dilation(modelSize), true), 64, 64)
                .createModel(), true);
        models.add(m);
        return m;
    }

    @Override
    protected void scale(VillagerEntityMCA villager, MatrixStack matrixStackIn, float partialTickTime) {
        float height = villager.getScaleFactor();
        float width = villager.getHorizontalScaleFactor();
        matrixStackIn.scale(width, height, width);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(VillagerEntityMCA entity, boolean showBody, boolean translucent, boolean showOutlines) {
        //setting the type to null prevents it from rendering
        //we need a skin layer anyways because of the color
        return null;
    }

    @Override
    public void render(VillagerEntityMCA villager, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light) {
        AgeState ageState = villager.getAgeState();
        model.headSize = ageState.getHead();
        model.headWidth = model.headSize / ageState.getWidth();
        model.breastSize = villager.getGenetics().getBreastSize() * ageState.getBreasts();

        // also apply this to the layers models, not sure if this is the intended solution
        for (VillagerEntityBaseModelMCA<VillagerEntityMCA> m : models) {
            m.breastSize = this.model.breastSize;
            m.headSize = this.model.headSize;
            m.headWidth = this.model.headWidth;
        }

        super.render(villager, yaw, tickDelta, matrixStack, vertexConsumers, light);
    }

    @Override
    protected boolean hasLabel(VillagerEntityMCA villager) {
        return MinecraftClient.getInstance().player != null
                && MinecraftClient.getInstance().player.squaredDistanceTo(villager) < 25;
    }
}

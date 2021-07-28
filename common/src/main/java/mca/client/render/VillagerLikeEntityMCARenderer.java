package mca.client.render;

import mca.client.model.VillagerEntityBaseModelMCA;
import mca.client.model.VillagerEntityModelMCA;
import mca.entity.Infectable;
import mca.entity.VillagerLike;
import mca.util.compat.model.Dilation;
import mca.util.compat.model.TexturedModelData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;

import org.jetbrains.annotations.Nullable;

public class VillagerLikeEntityMCARenderer<T extends MobEntity & VillagerLike<T>> extends BipedEntityRenderer<T, VillagerEntityModelMCA<T>> {
    public VillagerLikeEntityMCARenderer(EntityRenderDispatcher ctx, VillagerEntityModelMCA<T> model) {
        super(ctx, model, 0.5F);
        addFeature(new ArmorFeatureRenderer<>(this, createArmorModel(0.5f), createArmorModel(1.0f)));
    }

    private VillagerEntityBaseModelMCA<T> createArmorModel(float modelSize) {
        return new VillagerEntityBaseModelMCA<>(
                TexturedModelData.of(
                        VillagerEntityBaseModelMCA.getModelData(new Dilation(modelSize), true), 64, 32)
                .createModel(), true);
    }

    @Override
    protected void scale(T villager, MatrixStack matrices, float tickDelta) {
        float height = villager.getScaleFactor();
        float width = villager.getHorizontalScaleFactor();
        matrices.scale(width, height, width);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutlines) {
        //setting the type to null prevents it from rendering
        //we need a skin layer anyways because of the color
        return null;
    }

    @Override
    protected boolean hasLabel(T villager) {
        return MinecraftClient.getInstance().player != null
                && MinecraftClient.getInstance().player.squaredDistanceTo(villager) < 25;
    }

    @Override
    protected boolean isShaking(T entity) {
        return entity.getInfectionProgress() > Infectable.FEVER_THRESHOLD;
    }
}

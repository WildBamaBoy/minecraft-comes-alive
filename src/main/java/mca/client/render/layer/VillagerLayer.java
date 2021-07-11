package mca.client.render.layer;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public abstract class VillagerLayer<T extends VillagerEntityMCA, M extends VillagerEntityModelMCA<T>> extends FeatureRenderer<T, M> {

    private static final float[] DEFAULT_COLOR = new float[]{1, 1, 1};

    protected static final Map<String, Identifier> TEXTURE_CACHE = Maps.newHashMap();

    public final M model;

    public VillagerLayer(FeatureRendererContext<T, M> renderer, M model) {
        super(renderer);
        this.model = model;
    }

    @Nullable
    protected String getSkin(T entity) {
        return null;
    }

    @Nullable
    protected String getOverlay(T entity) {
        return null;
    }

    protected float[] getColor(T entity) {
        return DEFAULT_COLOR;
    }

    boolean isTranslucent() {
        return false;
    }

    private void renderModel(MatrixStack transform, VertexConsumerProvider buffer, int p_241738_3_, M model, float r, float g, float b, Identifier res, int overlay) {
        this.getContextModel().copyPropertiesTo(model);

        VertexConsumer ivertexbuilder = buffer.getBuffer(isTranslucent() ? RenderLayer.getEntityTranslucent(res) : RenderLayer.getEntityCutoutNoCull(res));
        model.render(transform, ivertexbuilder, p_241738_3_, overlay, r, g, b, 1.0F);
    }

    @Override
    public void render(MatrixStack transform, VertexConsumerProvider buffer, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        //copy the animation to this layers model
        getContextModel().copyPropertiesTo(model);

        //texture
        String p = getSkin(entity);
        if (!Strings.isNullOrEmpty(p)) {
            //color
            float[] color = getColor(entity);

            Identifier res = getResource(p);
            this.renderModel(transform, buffer, light, model, color[0], color[1], color[2], res, LivingEntityRenderer.getOverlay(entity, 0));
        }

        //overlay
        p = getOverlay(entity);
        if (!Strings.isNullOrEmpty(p)) {
            Identifier res = getResource(p);
            this.renderModel(transform, buffer, light, model, 1, 1, 1, res, LivingEntityRenderer.getOverlay(entity, 0));
        }
    }

    private Identifier getResource(String name) {
        return TEXTURE_CACHE.computeIfAbsent(name, s -> {
            try {
                return new Identifier(s);
            } catch (InvalidIdentifierException ignored) {
                return new Identifier("");
            }
        });
    }
}
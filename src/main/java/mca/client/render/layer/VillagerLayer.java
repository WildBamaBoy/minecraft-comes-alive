package mca.client.render.layer;

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

public abstract class VillagerLayer<T extends VillagerEntityMCA, M extends VillagerEntityModelMCA<T>> extends FeatureRenderer<T, M> {
    protected static final Map<String, Identifier> textureRes = Maps.newHashMap();
    public final M model;

    public VillagerLayer(FeatureRendererContext<T, M> renderer, M model) {
        super(renderer);
        this.model = model;
    }

    String getTexture(T entity) {
        return null;
    }

    String getOverlayTexture(T entity) {
        return null;
    }

    float[] getColor(T entity) {
        return new float[]{1.0f, 1.0f, 1.0f};
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
    public void render(MatrixStack transform, VertexConsumerProvider buffer, int p_225628_3_, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        String p;

        //copy the animation to this layers model
        getContextModel().copyPropertiesTo(model);

        //texture
        p = getTexture(entity);
        if (p != null && p.length() > 0) {
            //color
            float[] color = getColor(entity);

            Identifier res = getResource(p);
            this.renderModel(transform, buffer, p_225628_3_, model, color[0], color[1], color[2], res, LivingEntityRenderer.getOverlay(entity, 0.0F));
        }

        //overlay
        p = getOverlayTexture(entity);
        if (p != null && p.length() > 0) {
            Identifier res = getResource(p);
            this.renderModel(transform, buffer, p_225628_3_, model, 1.0f, 1.0f, 1.0f, res, LivingEntityRenderer.getOverlay(entity, 0.0F));
        }
    }

    private Identifier getResource(String s) {
        Identifier resourcelocation = textureRes.get(s);
        if (resourcelocation == null) {
            try {
                resourcelocation = new Identifier(s);
            } catch (InvalidIdentifierException ignored) {
                resourcelocation = new Identifier("");
            }
            textureRes.put(s, resourcelocation);
        }
        return resourcelocation;
    }
}
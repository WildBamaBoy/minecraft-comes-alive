package mca.client.render.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mca.client.model.VillagerEntityModelMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class VillagerLayer<T extends VillagerEntityMCA, M extends VillagerEntityModelMCA<T>> extends LayerRenderer<T, M> {
    protected static final Map<String, ResourceLocation> textureRes = Maps.newHashMap();
    public final M model;

    public VillagerLayer(IEntityRenderer<T, M> renderer, M model) {
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

    private void renderModel(MatrixStack transform, IRenderTypeBuffer buffer, int p_241738_3_, M model, float r, float g, float b, ResourceLocation res, int overlay) {
        this.getParentModel().copyPropertiesTo(model);

        IVertexBuilder ivertexbuilder = buffer.getBuffer(isTranslucent() ? RenderType.entityTranslucent(res) : RenderType.entityCutoutNoCull(res));
        model.renderToBuffer(transform, ivertexbuilder, p_241738_3_, overlay, r, g, b, 1.0F);
    }

    @Override
    public void render(MatrixStack transform, IRenderTypeBuffer buffer, int p_225628_3_, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        String p;

        //copy the animation to this layers model
        getParentModel().copyPropertiesTo(model);

        //texture
        p = getTexture(entity);
        if (p != null && p.length() > 0) {
            //color
            float[] color = getColor(entity);

            ResourceLocation res = getResource(p);
            this.renderModel(transform, buffer, p_225628_3_, model, color[0], color[1], color[2], res, LivingRenderer.getOverlayCoords(entity, 0.0F));
        }

        //overlay
        p = getOverlayTexture(entity);
        if (p != null && p.length() > 0) {
            ResourceLocation res = getResource(p);
            this.renderModel(transform, buffer, p_225628_3_, model, 1.0f, 1.0f, 1.0f, res, LivingRenderer.getOverlayCoords(entity, 0.0F));
        }
    }

    private ResourceLocation getResource(String s) {
        ResourceLocation resourcelocation = textureRes.get(s);
        if (resourcelocation == null) {
            try {
                resourcelocation = new ResourceLocation(s);
            } catch (ResourceLocationException ignored) {
                resourcelocation = new ResourceLocation("");
            }
            textureRes.put(s, resourcelocation);
        }
        return resourcelocation;
    }
}
package mca.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class LayerVillager<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    protected M model;

    protected static final Map<String, ResourceLocation> textureRes = Maps.newHashMap();

    public LayerVillager(IEntityRenderer<T, M> renderer, M model) {
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

    private void renderModel(MatrixStack transform, IRenderTypeBuffer buffer, int p_241738_3_, boolean p_241738_5_, M model, float r, float g, float b, ResourceLocation res) {
        this.getParentModel().copyPropertiesTo(model);

        IVertexBuilder ivertexbuilder = buffer.getBuffer(isTranslucent() ? RenderType.entityTranslucent(res) : RenderType.entityCutoutNoCull(res));
        model.renderToBuffer(transform, ivertexbuilder, p_241738_3_, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    @Override
    public void render(MatrixStack transform, IRenderTypeBuffer buffer, int p_225628_3_, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        String p;

        //texture
        p = getTexture(entity);
        if (p != null && p.length() > 0) {
            //color
            float[] color = getColor(entity);

            ResourceLocation res = getClothingResource(p);
            this.renderModel(transform, buffer, p_225628_3_, false, model, color[0], color[1], color[2], res);
        }

        //overlay
        p = getOverlayTexture(entity);
        if (p != null && p.length() > 0) {
            ResourceLocation res = getClothingResource(p);
            this.renderModel(transform, buffer, p_225628_3_, false, model, 1.0f, 1.0f, 1.0f, res);
        }
    }

    private ResourceLocation getClothingResource(String s) {
        ResourceLocation resourcelocation = textureRes.get(s);
        if (resourcelocation == null) {
            resourcelocation = new ResourceLocation(s);
            textureRes.put(s, resourcelocation);
        }
        return resourcelocation;
    }
}
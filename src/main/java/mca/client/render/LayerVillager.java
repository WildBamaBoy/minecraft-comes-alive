package mca.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mca.client.model.ModelVillagerMCA;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class LayerVillager<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends LayerRenderer<T, M> {
    protected A model;

    protected static final Map<String, ResourceLocation> textureRes = Maps.newHashMap();

    public LayerVillager(IEntityRenderer<T, M> renderer, A model) {
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

    @Override
    public void render(MatrixStack transform, IRenderTypeBuffer buffer, int p_225628_3_, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
//        model.setModelAttributes(this.renderer.getMainModel());
//        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
//
//        String p;
//
//        //texture
//        p = getTexture(entity);
//        if (p != null && p.length() > 0) {
//            ResourceLocation res = getClothingResource(p);
//            this.renderer.bindTexture(res);
//
//            //color
//            float[] color = getColor(entity);
//            GlStateManager.color(color[0], color[1], color[2], 1.0f);
//
//            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
//            IVertexBuilder ivertexbuilder = ItemRenderer.getArmorFoilBuffer(p_241738_2_, RenderType.armorCutoutNoCull(armorResource), false, p_241738_5_);
//            model.renderToBuffer(transform, buffer, p_225628_5_, p_225628_6_, p_225628_7_, p_225628_8_, p_225628_9_, p_225628_10_);
//        }
//
//        //overlay
//        p = getOverlayTexture(entity);
//        if (p != null && p.length() > 0) {
//            ResourceLocation res = getClothingResource(p);
//            this.renderer.bindTexture(res);
//
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
//            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
//        }
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
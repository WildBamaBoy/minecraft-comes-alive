package mca.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import mca.client.model.ModelVillagerMCA;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class LayerVillager implements LayerRenderer<LivingEntity> {
    protected Model model;

    private final LivingRenderer<?> renderer;
    protected static final Map<String, ResourceLocation> textureRes = Maps.newHashMap();

    public LayerVillager(LivingRenderer<?> rendererIn, float offset, float offsetHead) {
        this.renderer = rendererIn;
        this.model = new ModelVillagerMCA(offset, offsetHead, true);
    }

    String getTexture(LivingEntity entity) {
        return null;
    }

    String getOverlayTexture(LivingEntity entity) {
        return null;
    }

    float[] getColor(LivingEntity entity) {
        return new float[]{1.0f, 1.0f, 1.0f};
    }

    @ParametersAreNonnullByDefault
    public void doRenderLayer(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        model.setModelAttributes(this.renderer.getMainModel());
        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);

        String p;

        //texture
        p = getTexture(entity);
        if (p != null && p.length() > 0) {
            ResourceLocation res = getClothingResource(p);
            this.renderer.bindTexture(res);

            //color
            float[] color = getColor(entity);
            GlStateManager.color(color[0], color[1], color[2], 1.0f);

            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        //overlay
        p = getOverlayTexture(entity);
        if (p != null && p.length() > 0) {
            ResourceLocation res = getClothingResource(p);
            this.renderer.bindTexture(res);

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
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
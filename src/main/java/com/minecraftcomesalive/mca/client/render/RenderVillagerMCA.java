package com.minecraftcomesalive.mca.client.render;

import cobalt.util.ResourceLocationCache;
import com.minecraftcomesalive.mca.client.model.ModelVillagerMCA;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderVillagerMCA extends BipedRenderer<EntityVillagerMCA, ModelVillagerMCA<EntityVillagerMCA>> {
    public RenderVillagerMCA(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new ModelVillagerMCA<>(), 0.7F);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityVillagerMCA entity) {
        return ResourceLocationCache.get(entity.getTexture());
    }

    @Override
    protected void preRenderCallback(EntityVillagerMCA villager, MatrixStack matrixStackIn, float partialTickTime) {
        float scale = 0.9375F;
        matrixStackIn.scale(scale, scale, scale);
    }
}

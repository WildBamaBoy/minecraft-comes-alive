package mca.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.client.model.ModelGrimReaper;
import mca.entity.EntityGrimReaper;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderGrimReaper extends BipedRenderer<EntityGrimReaper, ModelGrimReaper<EntityGrimReaper>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("mca:textures/entity/grimreaper.png");

    public RenderGrimReaper(EntityRendererManager manager) {
        super(manager, new ModelGrimReaper<>(), 0.5F);
    }


    @Override
    protected void scale(EntityGrimReaper reaper, MatrixStack matrixStackIn, float partialTickTime) {
        float scale = 1.3f;
        matrixStackIn.scale(scale, scale, scale);

        model.reaperState = reaper.getAttackState();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGrimReaper reaper) {
        return TEXTURE;
    }
}
package mca.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.client.model.GrimReaperEntityModel;
import mca.entity.GrimReaperEntity;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class GrimReaperRenderer extends BipedRenderer<GrimReaperEntity, GrimReaperEntityModel<GrimReaperEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("mca:textures/entity/grimreaper.png");

    public GrimReaperRenderer(EntityRendererManager manager) {
        super(manager, new GrimReaperEntityModel<>(), 0.5F);
    }


    @Override
    protected void scale(GrimReaperEntity reaper, MatrixStack matrixStackIn, float partialTickTime) {
        float scale = 1.3f;
        matrixStackIn.scale(scale, scale, scale);

        model.reaperState = reaper.getAttackState();
    }

    @Override
    public ResourceLocation getTextureLocation(GrimReaperEntity reaper) {
        return TEXTURE;
    }
}
package mca.client.render;

import mca.client.model.GrimReaperEntityModel;
import mca.entity.GrimReaperEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GrimReaperRenderer extends BipedEntityRenderer<GrimReaperEntity, GrimReaperEntityModel<GrimReaperEntity>> {
    private static final Identifier TEXTURE = new Identifier("mca:textures/entity/grimreaper.png");

    public GrimReaperRenderer(EntityRenderDispatcher manager) {
        super(manager, new GrimReaperEntityModel<>(), 0.5F);
    }


    @Override
    protected void scale(GrimReaperEntity reaper, MatrixStack matrixStackIn, float partialTickTime) {
        float scale = 1.3f;
        matrixStackIn.scale(scale, scale, scale);

        model.reaperState = reaper.getAttackState();
    }

    @Override
    public Identifier getTextureLocation(GrimReaperEntity reaper) {
        return TEXTURE;
    }
}
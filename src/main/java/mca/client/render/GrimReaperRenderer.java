package mca.client.render;

import mca.client.model.GrimReaperEntityModel;
import mca.entity.GrimReaperEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GrimReaperRenderer extends BipedEntityRenderer<GrimReaperEntity, GrimReaperEntityModel<GrimReaperEntity>> {
    private static final Identifier TEXTURE = new Identifier("mca:textures/entity/grimreaper.png");

    public GrimReaperRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new GrimReaperEntityModel<>(
            TexturedModelData.of(GrimReaperEntityModel.getModelData(Dilation.NONE), 64, 64).createModel()
        ), 0.5F);
    }

    @Override
    protected void scale(GrimReaperEntity reaper, MatrixStack matrices, float tickDelta) {
        matrices.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    public Identifier getTexture(GrimReaperEntity reaper) {
        return TEXTURE;
    }
}
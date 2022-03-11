package mca.client.render;

import java.util.List;

import mca.block.TombstoneBlock;
import mca.block.TombstoneBlock.Data;
import mca.util.compat.TextRendererCompat;
import mca.util.localization.FlowingText;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class TombstoneBlockEntityRenderer extends BlockEntityRenderer<TombstoneBlock.Data> {

    public TombstoneBlockEntityRenderer(BlockEntityRenderDispatcher context) {
        super(context);
    }

    @Override
    public void render(Data entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        if (!entity.hasEntity()) {
            return;
        }

        BlockState state = entity.getCachedState();

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);

        Direction facing = state.get(Properties.HORIZONTAL_FACING).getOpposite();

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-facing.asRotation()));
        matrices.translate(0, 0, 0);
        matrices.scale(0.010416667F, 0.010416667F, 0.010416667F);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));

        TombstoneBlock block = (TombstoneBlock)state.getBlock();

        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(block.getRotation()));

        Vec3d offset = block.getNameplateOffset();
        matrices.translate(offset.getX(), offset.getY(), offset.getZ());

        int maxLineWidth = block.getLineWidth();

        TextRenderer text = dispatcher.getTextRenderer();

        float y = drawText(text, text.wrapLines(new TranslatableText("block.mca.tombstone.header"), maxLineWidth), 0, matrices, vertexConsumers, light);

        y += 5;

        FlowingText name = entity.getOrCreateEntityName(n -> FlowingText.Factory.wrapLines(text, n, maxLineWidth - 10, block.getMaxNameHeight()));

        matrices.push();
        matrices.scale(name.scale(), name.scale(), name.scale());

        y = drawText(text, name.lines(), y / name.scale(), matrices, vertexConsumers, light) * name.scale();

        matrices.pop();

        y += 5;

        drawText(text, text.wrapLines(new TranslatableText("block.mca.tombstone.footer." + entity.getGender().binary().getStrName()), maxLineWidth), y, matrices, vertexConsumers, light);

        matrices.pop();
    }

    private float drawText(TextRenderer text, List<OrderedText> lines, float y, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        for (OrderedText line : lines) {
            float x = -text.getWidth(line) / 2F;

            TextRendererCompat.drawWithOutline(text, line, x, y, 0xFFFFFF, 0x000000, matrices.peek().getModel(), vertexConsumers, light);

            y += 10;
        }

        return y;
    }
}

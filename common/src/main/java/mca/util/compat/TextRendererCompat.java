package mca.util.compat;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Matrix4f;

public class TextRendererCompat {
    private static final OutlinedDrawer DRAWER = new OutlinedDrawer();
    /**
     * Draws text with an outline.
     *
     * @since MC 1.17
     * @see FontRenderer#drawWithOutline
     */
    public static void drawWithOutline(TextRenderer renderer, OrderedText text, float x, float y, int fillColor, int borderColor, Matrix4f transformation, VertexConsumerProvider provider, int light) {
        DRAWER.drawWithOutline(renderer, text, x, y, fillColor, borderColor, transformation, light);
    }

    private static class OutlinedDrawer implements OrderedText {
        private OrderedText text;
        private VertexConsumerProvider.Immediate immediate;
        private TextRenderer renderer;
        private int l;
        private float x;
        private float y;

        void drawWithOutline(TextRenderer renderer, OrderedText text, float x, float y, int fillColor, int borderColor, Matrix4f transformation, int light) {
            // we do it like this to avoid any extra allocations
            this.text = text;
            this.renderer = renderer;
            // we use our own immediate to draw without affecting the rest of the world
            this.immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            this.x = x;
            this.y = y;
            this.l = tweakTransparency(borderColor);

            // intercept the call and draw our shadow
            for(int m = -1; m <= 1; ++m) { // left|middle|right
                for(int n = -1; n <= 1; ++n) { // top|middle|bottom
                    if (m != 0 || n != 0) { // skip the middle one
                        float[] fs = new float[]{x};
                        int[] fs2 = new int[] {m, n}; // changed from mojang: since m and n can't be read inside the lambda.
                        renderer.draw(this, x + m, y + n, 0xFF000000, false, transformation, immediate, false, 0, light);
                    }
                }
            }

            renderer.draw(this, x, y, fillColor, false, transformation, immediate, false, 0, light);

            // mojang uses a render layer with polygon offseting to keep the layers from z-fighting
            // we don't have that, so we have to apply the offset manually
            RenderSystem.polygonOffset(-1, -10);
            RenderSystem.enablePolygonOffset();
            immediate.draw();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();

            this.immediate = null;
            this.renderer = null;
            this.text = null;
        }

        @Override
        public boolean accept(CharacterVisitor visitor) {
            immediate.draw();
            return text.accept(visitor);
        }

        static int tweakTransparency(int argb) {
            return (argb & -67108864) == 0 ? argb | -16777216 : argb;
        }
    }
}

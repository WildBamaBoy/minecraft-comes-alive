package mca.util.compat;

import com.mojang.blaze3d.systems.RenderSystem;

import mca.mixin.client.MixinTextRenderer;
import mca.mixin.client.MixinTextRenderer_Drawer;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TextColor;
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
            MixinTextRenderer_Drawer drawer = (MixinTextRenderer_Drawer)visitor;

            float originalX = drawer.getX();
            float originalY = drawer.getY();

            // changed from mojang: original used Style#withColor(int) but that's not available yet
            TextColor tc = TextColor.fromRgb(l);

            // intercept the call and draw our shadow
            for(int m = -1; m <= 1; ++m) { // left|middle|right
                for(int n = -1; n <= 1; ++n) { // top|middle|bottom
                    if (m != 0 || n != 0) { // skip the middle one
                        float[] fs = new float[]{x};
                        int[] fs2 = new int[] {m, n}; // changed from mojang: since m and n can't be read inside the lambda.
                        text.accept((lx, style, mx) -> {
                            boolean bl = style.isBold();
                            FontStorage fontStorage = ((MixinTextRenderer)renderer).invokeGetFontStorage(style.getFont());
                            Glyph glyph = fontStorage.getGlyph(mx);
                            drawer.setX(fs[0] + fs2[0] * glyph.getShadowOffset());
                            drawer.setY(y + fs2[1] * glyph.getShadowOffset());
                            fs[0] += glyph.getAdvance(bl);
                            return visitor.accept(lx, style.withColor(tc), mx);
                        });
                    }
                }
            }

            immediate.draw();

            // reset the drawer's position so the actual text is rendered where it's meant to
            drawer.setX(originalX);
            drawer.setY(originalY);

            return text.accept(visitor);
        }

        static int tweakTransparency(int argb) {
            return (argb & -67108864) == 0 ? argb | -16777216 : argb;
        }
    }
}

package mca.client.gui.widget;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class RectangleWidget extends DrawableHelper {
    public static void drawRectangle(MatrixStack transform, int x0, int y0, int x1, int y1, int color) {
        fill(transform, x0 + 1, y0, x1, y0 + 1, color);
        fill(transform, x1 - 1, y0 + 1, x1, y1, color);
        fill(transform, x0, y1 - 1, x1, y1, color);
        fill(transform, x0, y0, x0 + 1, y1, color);
    }
}

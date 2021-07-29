package mca.client.resources;

import java.util.HashMap;
import java.util.Map;

import mca.MCA;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ColorPalette {
    static final Map<Identifier, ColorPalette> REGISTRY = new HashMap<>();

    public static final ColorPalette SKIN = new ColorPalette(new Identifier(MCA.MOD_ID, "textures/colormap/villager_skin.png"));
    public static final ColorPalette HAIR = new ColorPalette(new Identifier(MCA.MOD_ID, "textures/colormap/villager_hair.png"));

    static final Data EMPTY = new Data(1, 1, new int[] { 0xFFFFFF });

    private final Identifier id;

    Data data = EMPTY;

    public ColorPalette(Identifier id) {
        this.id = id;
        REGISTRY.put(id, this);
    }

    public Identifier getId() {
        return id;
    }

    public float[] getColor(float u, float v, float greenShift) {
        int x = clampFloor(v, data.width - 1); //horizontal
        int y = clampFloor(u, data.height - 1); // vertical

        int color = data.colors[y * data.height + x];

        float[] result = new float[] {
                NativeImage.getBlue(color) / 255F,
                NativeImage.getGreen(color) / 255F,
                NativeImage.getRed(color) / 255F
        };

        if (greenShift > 0) {
            applyGreenShift(result, greenShift / 255F);
        }

        return result;
    }

    private static void applyGreenShift(float[] color, float greenShift) {

        float pecentDown = 1 - greenShift / 1.8F;
        float perfenctUp = 1 + greenShift / 2F;

        color[0] *= pecentDown;
        color[1] *= perfenctUp;
        color[2] *= pecentDown;
    }

    private static int clampFloor(float v, int max) {
        return (int)Math.floor(MathHelper.clamp(v * max, 0, max));
    }

    static class Data {
        int width;
        int height;

        int[] colors;

        public Data(int width, int height, int[] colors) {
            this.width = width;
            this.height = height;
            this.colors = colors;
        }
    }
}






















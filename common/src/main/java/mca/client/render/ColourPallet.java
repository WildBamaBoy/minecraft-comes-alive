package mca.client.render;

public class ColourPallet {

    private final double[][][] colors;

    public ColourPallet(double[][][] colors) {
        this.colors = colors;
    }

    public float[] getColor(float u, float v, float greenShift) {
        int m = clampFloor(u, colors.length);
        int h = clampFloor(v, colors[0].length);
        double[] color = colors[m][h];

        float[] result = new float[] {
                (float) color[0],
                (float) color[1],
                (float) color[2]
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

    private static int clampFloor(double v, int max) {
        return (int) Math.min(max - 1, Math.max(0, Math.floor(v * max)));
    }
}

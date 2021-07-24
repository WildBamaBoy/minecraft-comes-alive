package mca.util.compat.model;

/**
 * @since MC 1.17
 */
public class ModelPartBuilder {
    public static ModelPartBuilder create() {
        return new ModelPartBuilder();
    }

    private boolean mirror;

    ModelPartCompat part = new ModelPartCompat();

    public ModelPartBuilder uv(int u, int v) {
        part.setTextureOffset(u, v);
        return this;
    }

    public ModelPartBuilder cuboid(float x, float y, float z, float w, float h, float d, Dilation dilation, float textureScaleX, float textureScaleY) {
        part.setTextureSize((int)(64 * textureScaleX), (int)(64 * textureScaleY));
        cuboid(x, y, z, w, h, d, dilation);
        part.setTextureSize(64, 64);
        return this;
    }

    public ModelPartBuilder cuboid(float x, float y, float z, float w, float h, float d, Dilation dilation) {
        part.addCuboid(x, y, z, w, h, d, dilation.value, mirror);
        return this;
    }

    public ModelPartBuilder mirrored() {
        this.mirror = true;
        return this;
    }
}

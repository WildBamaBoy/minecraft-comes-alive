package mca.util.compat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since MC 1.17
 */
public class ModelPartBuilder {
    public static ModelPartBuilder create() {
        return new ModelPartBuilder();
    }

    private boolean mirror;
    private int u, v;

    final ModelPartCompat part = new ModelPartCompat();

    private final List<Runnable> cuboids = new ArrayList<>();
    final Map<String, ModelPartData> children = new HashMap<>();

    public ModelPartBuilder uv(int u, int v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public ModelPartBuilder cuboid(float x, float y, float z, float w, float h, float d, Dilation dilation, float textureScaleX, float textureScaleY) {
        boolean mirror = this.mirror;
        int u = this.u;
        int v = this.v;
        cuboids.add(() -> {
            int texW = part.getTextureWidth();
            int texH = part.getTextureHeight();
            part.setTextureOffset(u, v);
            part.setTextureSize((int)(texW * textureScaleX), (int)(texH * textureScaleY));
            part.addCuboid(x, y, z, w, h, d, dilation.value, mirror);
            part.setTextureSize(texW, texH);
        });
        return this;
    }

    public ModelPartBuilder cuboid(float x, float y, float z, float w, float h, float d, Dilation dilation) {
        return cuboid(x, y, z, w, h, d, dilation, 1, 1);
    }

    public ModelPartBuilder mirrored() {
        this.mirror = true;
        return this;
    }

    ModelPartCompat build(int textureWidth, int textureHeight) {
        part.setTextureSize(textureWidth, textureHeight);
        cuboids.forEach(Runnable::run);
        children.values().forEach(v -> v.builder.build(textureWidth, textureHeight));
        return part;
    }
}

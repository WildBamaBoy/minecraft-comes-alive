package mca.util.compat.model;

/**
 * @since MC 1.17
 */
public interface TexturedModelData {
    ModelPartCompat createModel();

    static TexturedModelData of(ModelData data, int w, int h) {
        return () -> data.getRoot().builder.build(w, h);
    }
}

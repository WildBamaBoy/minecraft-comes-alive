package mca.util.compat.model;

/**
 * @since MC 1.17
 */
public class Dilation {
    public static Dilation NONE = new Dilation(0);

    float value;

    public Dilation(float value) {
        this.value = value;
    }

    public Dilation add(float amount) {
        return new Dilation(value + amount);
    }
}

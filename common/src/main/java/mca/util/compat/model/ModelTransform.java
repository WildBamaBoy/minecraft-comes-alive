package mca.util.compat.model;

/**
 * @since MC 1.17
 */
public class ModelTransform {
    public static final ModelTransform NONE = of(0, 0, 0, 0, 0, 0);
    public final float pivotX, pivotY, pivotZ;
    public final float pitch, yaw, roll;

    private ModelTransform(float pivotX, float pivotY, float pivotZ, float pitch, float yaw, float roll) {
       this.pivotX = pivotX;
       this.pivotY = pivotY;
       this.pivotZ = pivotZ;
       this.pitch = pitch;
       this.yaw = yaw;
       this.roll = roll;
    }

    public static ModelTransform pivot(float pivotX, float pivotY, float pivotZ) {
       return of(pivotX, pivotY, pivotZ, 0, 0, 0);
    }

    public static ModelTransform rotation(float pitch, float yaw, float roll) {
       return of(0, 0, 0, pitch, yaw, roll);
    }

    public static ModelTransform of(float pivotX, float pivotY, float pivotZ, float pitch, float yaw, float roll) {
       return new ModelTransform(pivotX, pivotY, pivotZ, pitch, yaw, roll);
    }
 }
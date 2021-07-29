package mca.util.compat.model;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.minecraft.client.model.ModelPart;

/**
 * @since MC 1.17
 */
public class ModelPartCompat extends net.minecraft.client.model.ModelPart {

    final Map<String, ModelPartCompat> children = new HashMap<>();

    Dilation dilation =  Dilation.NONE;

    private int textureWidth = 64;
    private int textureHeight = 64;

    ModelPartCompat() {
        super(64, 64, 0, 0);
    }

    @Override
    public ModelPart setTextureSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return super.setTextureSize(width, height);
    }

    public float getOriginalDilation() {
        return dilation.value;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public ModelPartCompat getChild(String name) {
        ModelPartCompat element = children.get(name);
        if (element == null) {
            throw new NoSuchElementException("Can't find part " + name);
        }
        return element;
    }

    public ModelTransform getTransform() {
        return ModelTransform.of(pivotX, pivotY, pivotZ, pitch, yaw, roll);
    }

    public void setAngles(float pitch, float yaw, float roll) {
        setAngles(this, pitch, yaw, roll);
    }

    public void setTransform(ModelTransform transform) {
        setAngles(transform.pitch, transform.yaw, transform.roll);
        setPivot(transform.pivotX, transform.pivotY, transform.pivotZ);
    }

    public static void setAngles(ModelPart part, float pitch, float yaw, float roll) {
        part.pitch = pitch;
        part.yaw = yaw;
        part.roll = roll;
    }
}

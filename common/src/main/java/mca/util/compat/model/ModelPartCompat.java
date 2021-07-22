package mca.util.compat.model;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.minecraft.client.model.ModelPart;

/**
 * @since MC 1.17
 */
public class ModelPartCompat extends net.minecraft.client.model.ModelPart {

    final Map<String, ModelPartData> children = new HashMap<>();

    ModelPartCompat() {
        super(64, 64, 0, 0);
    }

    public ModelPartCompat getChild(String name) {
        ModelPartData element = children.get(name);
        if (element == null) {
            throw new NoSuchElementException("Can't find part " + name);
        }
        return element.part;
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

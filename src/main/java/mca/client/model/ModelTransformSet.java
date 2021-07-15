package mca.client.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.util.math.MathHelper;

public interface ModelTransformSet {
    interface Op {
        Op KEEP = (delta, a, b) -> a;
        Op SET = (delta, a, b) -> b;
        Op ADD = (delta, a, b) -> a + b;
        Op LERP = MathHelper::lerp;

        float apply(float delta, float a, float b);
    }

    Transformer get(String key);

    default ModelTransformSet interpolate(ModelTransformSet to, float delta) {
        if (delta <= 0) return this; // skip on the ends
        if (delta >= 1) return to;
        // variables can't be modified by lambdas, but arrays can
        Transformer[] components = new Transformer[2];
        Transformer combined = (part, op, scale) -> {
            components[0].applyTo(part, Op.LERP, delta);
            components[1].applyTo(part, Op.LERP, 1 - delta);
        };
        return key -> {
            components[0] = Preconditions.checkNotNull(get(key), "Cannot interpolate because the source set was missing key `" + key + "`");
            components[1] = Preconditions.checkNotNull(to.get(key), "Cannot interpolate because the target set was missing key `" + key + "`");
            return combined;
        };
    }

    interface Transformer {
        default void applyTo(ModelPart part) {
            applyTo(part, Op.SET, 1);
        }

        void applyTo(ModelPart part, Op op, float scale);
    }

    static class Builder {
        private static final float TO_RADIANS = (float)Math.PI / 180F;
        private final Map<String, Transformer> transforms = new HashMap<>();

        public Builder with(String key, float x, float y, float z, float pitch, float yaw, float roll) {
            return with(key, x, y, z, pitch, yaw, roll, Op.SET, Op.SET);
        }

        public Builder with(String key, float x, float y, float z, float pitch, float yaw, float roll, Op pivot) {
            return with(key, x, y, z, pitch, yaw, roll, pivot, Op.SET);
        }

        public Builder rotate(String key, float pitch, float yaw, float roll) {
            return rotate(key, pitch, yaw, roll, Op.SET);
        }

        public Builder rotate(String key, float pitch, float yaw, float roll, Op op) {
            return with(key, 0, 0, 0, pitch, yaw, roll, Op.KEEP, op);
        }

        public Builder with(String key, float x, float y, float z, float pitch, float yaw, float roll, Op pivot, Op rotate) {
            ModelTransform transform = createTransform(x, y, z, pitch, yaw, roll);
            transforms.put(key, (part, op, delta) -> {
                part.pivotX = op.apply(delta, part.pivotX, pivot.apply(delta, part.pivotX, transform.pivotX));
                part.pivotY = op.apply(delta, part.pivotY, pivot.apply(delta, part.pivotY, transform.pivotY));
                part.pivotZ = op.apply(delta, part.pivotZ, pivot.apply(delta, part.pivotZ, transform.pivotZ));
                part.pitch = op.apply(delta, part.pitch, rotate.apply(delta, part.pitch, transform.pitch));
                part.yaw = op.apply(delta, part.yaw, rotate.apply(delta, part.yaw, transform.yaw));
                part.roll = op.apply(delta, part.roll, rotate.apply(delta, part.roll, transform.roll));
            });
            return this;
        }

        public static ModelTransform createTransform(float x, float y, float z, float pitch, float yaw, float roll) {
            return ModelTransform.of(x, y, z, pitch * TO_RADIANS, yaw * TO_RADIANS, roll * TO_RADIANS);
        }

        public ModelTransformSet build() {
            return new HashMap<>(transforms)::get;
        }
    }
}

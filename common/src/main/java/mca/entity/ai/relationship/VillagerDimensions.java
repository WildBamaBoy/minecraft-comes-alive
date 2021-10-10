package mca.entity.ai.relationship;

import net.minecraft.util.math.MathHelper;

public interface VillagerDimensions {
    float getWidth();

    float getHeight();

    float getBreasts();

    float getHead();

    final class Mutable implements VillagerDimensions {
        private float width;
        private float height;
        private float breasts;
        private float head;

        public Mutable(VillagerDimensions dimensions) {
            set(dimensions);
        }

        @Override
        public float getWidth() {
            return width;
        }

        @Override
        public float getHeight() {
            return height;
        }

        @Override
        public float getBreasts() {
            return breasts;
        }

        @Override
        public float getHead() {
            return head;
        }

        public void interpolate(VillagerDimensions a, VillagerDimensions b, float f) {
            width = MathHelper.lerp(f, a.getWidth(), b.getWidth());
            height = MathHelper.lerp(f, a.getHeight(), b.getHeight());
            breasts = MathHelper.lerp(f, a.getBreasts(), b.getBreasts());
            head = MathHelper.lerp(f, a.getHead(), b.getHead());
        }

        public void set(VillagerDimensions a) {
            width = a.getWidth();
            height = a.getHeight();
            breasts = a.getBreasts();
            head = a.getHead();
        }
    }
}

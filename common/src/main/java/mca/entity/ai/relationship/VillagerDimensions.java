package mca.entity.ai.relationship;

import net.minecraft.util.math.MathHelper;

public interface VillagerDimensions {
    float getWidth();

    float getHeight();

    float getBreasts();

    float getHead();

    public final class Mutable implements VillagerDimensions {
        private float width;
        private float height;
        private float breasts;
        private float head;

        public Mutable(VillagerDimensions dimensions) {
            set(dimensions, 1);
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

        public void set(VillagerDimensions to, float interp) {
            width = MathHelper.lerp(interp, getWidth(), to.getWidth());
            height = MathHelper.lerp(interp, getHeight(), to.getHeight());
            breasts = MathHelper.lerp(interp, getBreasts(), to.getBreasts());
            head = MathHelper.lerp(interp, getHead(), to.getHead());
        }
    }
}

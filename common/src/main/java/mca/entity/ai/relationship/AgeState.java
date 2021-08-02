package mca.entity.ai.relationship;

import mca.resources.API;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public enum AgeState implements VillagerDimensions {
    UNASSIGNED(1, 0.9F, 1, 1),
    BABY      (0.85F, 0.4F, 0, 1.5F),
    TODDLER   (0.85F, 0.5F, 0, 1.3F),
    CHILD     (0.85F, 0.7F, 0, 1.1F),
    TEEN      (0.85F, 0.8F, 0.5F, 1),
    ADULT     (1, 0.9F, 1, 1);

    public static final int MAX_AGE = -192000;
    public static final int STAGE_DURATION = -MAX_AGE / 4;

    private static final AgeState[] VALUES = values();

    private final float width;
    private final float height;
    private final float breasts;
    private final float head;

    AgeState(float width, float height, float breasts, float head) {
        this.width = width;
        this.height = height;
        this.breasts = breasts;
        this.head = head;
    }

    public Text getName() {
        return new TranslatableText("enum.agestate." + name().toLowerCase());
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

    public AgeState getNext() {
        if (this == ADULT) {
            return this;
        }
        return byId(ordinal() + 1);
    }

    public static AgeState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }

    public static AgeState random() {
        return byCurrentAge((int)(API.getRng().nextFloat() * MAX_AGE));
    }

    /**
     * Returns a float ranging from 0 to 1 representing the progress between stages.
     */
    public static float getDelta(float age) {
        return 1 - (-age % STAGE_DURATION) / STAGE_DURATION;
    }

    public static int getId(int age) {
        return MathHelper.clamp(1 + (age - MAX_AGE) / STAGE_DURATION, 0, 5);
    }

    public static AgeState byCurrentAge(int age) {

        if (age == MAX_AGE) {
            System.out.println(getId(age));
        }

        return byId(getId(age));
    }
}

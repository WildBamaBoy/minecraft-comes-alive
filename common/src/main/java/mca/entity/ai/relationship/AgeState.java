package mca.entity.ai.relationship;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum AgeState {
    UNASSIGNED(0.8f,  1.0f,  1.0f, 1.0f),
    BABY      (1.2f,  0.25f, 0.0f, 1.5f),
    TODDLER   (1.1f,  0.3f,  0.0f, 1.3f),
    CHILD     (1.0f,  0.5f,  0.0f, 1.1f),
    TEEN      (0.85f, 0.8f,  0.5f, 1.0f),
    ADULT     (1.0f,  0.9f,  1.0f, 1.0f);

    public static int startingAge = -192000;

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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getBreasts() {
        return breasts;
    }

    public float getHead() {
        return head;
    }

    public static AgeState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }

    public static AgeState byCurrentAge(int age) {
        int step = startingAge / 4;
        if (age >= step) {
            return AgeState.ADULT;
        } else if (age >= step * 2) {
            return AgeState.TEEN;
        } else if (age >= step * 3) {
            return AgeState.CHILD;
        } else if (age >= step * 4) {
            return AgeState.TODDLER;
        }
        return AgeState.BABY;
    }
}

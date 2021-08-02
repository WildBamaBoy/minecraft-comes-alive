package mca.entity.ai.relationship;

import mca.resources.API;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum AgeState {
    UNASSIGNED(1, 0.9F, 1, 1),
    BABY      (0.75F, 0.4F, 0, 1.5F),
    TODDLER   (0.75F, 0.5F, 0, 1.3F),
    CHILD     (0.75F, 0.7F, 0, 1.1F),
    TEEN      (0.85F, 0.8F, 0.5F, 1),
    ADULT     (1, 0.9F, 1, 1);

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

    public static AgeState random() {
        return byCurrentAge((int)(API.getRng().nextFloat() * startingAge));
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

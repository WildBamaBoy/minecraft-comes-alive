package mca.enums;

import mca.core.MCA;

import java.util.Arrays;
import java.util.Optional;

public enum EnumAgeState {
    UNASSIGNED(-1),
    BABY(0),
    TODDLER(1),
    CHILD(2),
    TEEN(3),
    ADULT(4);

    int id;

    EnumAgeState(int id) {
        this.id = id;
    }

    public static EnumAgeState byId(int id) {
        Optional<EnumAgeState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.isPresent() ? state.get() : UNASSIGNED;
    }

    public static EnumAgeState byCurrentAge(int startingAge, int growingAge) {
        int step = startingAge / 4;
        if (growingAge >= step) {
            return EnumAgeState.TEEN;
        } else if (growingAge >= step * 2) {
            return EnumAgeState.CHILD;
        } else if (growingAge >= step * 3 && growingAge < step * 2) {
            return EnumAgeState.TODDLER;
        }
        return EnumAgeState.ADULT;
    }

    public String localizedName() {
        return MCA.getLocalizer().localize("enum.agestate." + name().toLowerCase());
    }

    public int getId() {
        return id;
    }

    public float getWidth() {
        switch (this) {
            case BABY:
            case TODDLER:
                return 0.3F;
            case CHILD:
                return 0.5F;
            case TEEN:
                return 0.6F;
            case ADULT:
                return 0.8F;
            default:
                return 0.8F;
        }
    }

    public float getHeight() {
        switch (this) {
            case BABY:
                return 0.5F;
            case TODDLER:
                return 0.6F;
            case CHILD:
                return 1.1F;
            case TEEN:
                return 1.6F;
            case ADULT:
                return 2.0F;
            default:
                return 2.0F;
        }
    }

    public float getScaleForAge() {
        switch (this) {
            case BABY:
                return 0.35F;
            case TODDLER:
                return 0.50F;
            case CHILD:
                return 1.0F;
            case TEEN:
                return 1.35F;
            default:
                return 1.5F;
        }
    }
}

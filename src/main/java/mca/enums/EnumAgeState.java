package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.core.MCA;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum EnumAgeState {
    UNASSIGNED(-1, 0.8f, 2.0f, 1.5f),
    BABY(0, 0.3f, 0.5f, 0.4f),
    TODDLER(1, 0.3f, 0.6f, 0.5f),
    CHILD(2, 0.5f, 1.1f, 1f),
    TEEN(3, 0.6f, 1.6f, 1.35f),
    ADULT(4, 0.8f, 2f, 1.5f);

    int id;
    float width;
    float height;
    float scaleForAge;

    public static EnumAgeState byId(int id) {
        Optional<EnumAgeState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(UNASSIGNED);
    }

    public static EnumAgeState byCurrentAge(int startingAge, int growingAge) {
        int step = startingAge / 4;
        if (growingAge >= step) return EnumAgeState.TEEN;
        else if (growingAge >= step * 2) return EnumAgeState.CHILD;
        else if (growingAge >= step * 3 && growingAge < step * 2) return EnumAgeState.TODDLER;
        else if (growingAge >= step * 4 && growingAge < step * 3) return EnumAgeState.BABY;
        return EnumAgeState.ADULT;
    }

    public String localizedName() {
        return MCA.getLocalizer().localize("enum.agestate." + name().toLowerCase());
    }
}

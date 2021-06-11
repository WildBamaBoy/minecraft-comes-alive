package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.core.MCA;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum AgeState {
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

    public static AgeState byId(int id) {
        Optional<AgeState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(UNASSIGNED);
    }

    public static AgeState byCurrentAge(int age) {
        int startingAge = -192000;
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

    public String localizedName() {
        return MCA.localize("enum.agestate." + name().toLowerCase());
    }
}

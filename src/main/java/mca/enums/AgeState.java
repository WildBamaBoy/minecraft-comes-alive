package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.core.MCA;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum AgeState {
    UNASSIGNED(-1, 0.8f, 1.0f, 1.0f, 1.0f),
    BABY(0, 1.2f, 0.25f, 0.0f, 1.5f),
    TODDLER(1, 1.1f, 0.3f, 0.0f, 1.3f),
    CHILD(2, 1.0f, 0.5f, 0.0f, 1.1f),
    TEEN(3, 0.85f, 0.8f, 0.5f, 1.0f),
    ADULT(4, 1.0f, 0.9f, 1.0f, 1.0f);

    public static int startingAge = 192_000;

    int id;
    float width;
    float height;
    float breasts;
    float head;

    public static AgeState byId(int id) {
        Optional<AgeState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(UNASSIGNED);
    }

    public static AgeState byCurrentAge(int age) {
        int step = -startingAge / 4;
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

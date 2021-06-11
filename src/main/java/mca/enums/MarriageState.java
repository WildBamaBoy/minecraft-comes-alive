package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum MarriageState {
    NOT_MARRIED(0),
    ENGAGED(1),
    MARRIED(2),
    MARRIED_TO_PLAYER(3);

    int id;

    public static MarriageState byId(int id) {
        Optional<MarriageState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(NOT_MARRIED);
    }
}


package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum EnumMarriageState {
    NOT_MARRIED(0),
    ENGAGED(1),
    MARRIED(2);

    int id;

    public static EnumMarriageState byId(int id) {
        Optional<EnumMarriageState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(NOT_MARRIED);
    }
}


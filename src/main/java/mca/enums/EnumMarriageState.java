package mca.enums;

import java.util.Arrays;
import java.util.Optional;

public enum EnumMarriageState {
    NOT_MARRIED(0),
    ENGAGED(1),
    MARRIED(2);

    int id;

    EnumMarriageState(int id) {
        this.id = id;
    }

    public static EnumMarriageState byId(int id) {
        Optional<EnumMarriageState> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.isPresent() ? state.get() : NOT_MARRIED;
    }

    public int getId() {
        return id;
    }
}


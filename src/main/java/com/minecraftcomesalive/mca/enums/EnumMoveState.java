package com.minecraftcomesalive.mca.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum EnumMoveState {
    MOVE(0),
    FOLLOW(1),
    STAY(2);

    @Getter private int id;
    EnumMoveState(int id) {
        this.id = id;
    }

    public static EnumMoveState byId(int idIn) {
        Optional<EnumMoveState> state = Arrays.stream(values()).filter((e) -> e.id == idIn).findFirst();
        return state.orElse(MOVE);
    }
}

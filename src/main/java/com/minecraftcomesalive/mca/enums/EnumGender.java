package com.minecraftcomesalive.mca.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public enum EnumGender {
    UNASSIGNED(-1, "unassigned"),
    MALE(0, "male"),
    FEMALE(1, "female");

    @Getter private int id;
    @Getter private String name;

    EnumGender(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static EnumGender byId(int idIn) {
        Optional<EnumGender> state = Arrays.stream(values()).filter((e) -> e.id == idIn).findFirst();
        return state.orElse(UNASSIGNED);
    }

    public static EnumGender getRandom(Random rand) {
        return rand.nextBoolean() ? MALE : FEMALE;
    }

    public static EnumGender byName(String name) {
        Optional<EnumGender> state = Arrays.stream(values()).filter((e) -> e.name.equals(name)).findFirst();
        return state.orElse(UNASSIGNED);
    }
}

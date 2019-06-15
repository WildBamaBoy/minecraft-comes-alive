package mca.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public enum EnumGender {
    UNASSIGNED(0, "unassigned"),
    MALE(1, "male"),
    FEMALE(2, "female");

    int id;
    String name;

    EnumGender(int id, String str) {
        this.id = id;
        this.name = str;
    }

    public static EnumGender byId(int id) {
        Optional<EnumGender> gender = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return gender.isPresent() ? gender.get() : UNASSIGNED;
    }

    public static EnumGender getRandom() {
        return new Random().nextBoolean() ? MALE : FEMALE;
    }

    public static EnumGender byName(String name) {
        Optional<EnumGender> gender = Arrays.stream(values()).filter((e) -> e.getStrName().equals(name)).findFirst();
        return gender.isPresent() ? gender.get() : UNASSIGNED;
    }

    public int getId() {
        return id;
    }

    public String getStrName() {
        return name;
    }
}


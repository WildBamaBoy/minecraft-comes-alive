package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@AllArgsConstructor
@Getter
public enum EnumGender {
    UNASSIGNED(0, "unassigned"),
    MALE(1, "male"),
    FEMALE(2, "female");

    int id;
    String strName;

    public static EnumGender byId(int id) {
        Optional<EnumGender> gender = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return gender.orElse(UNASSIGNED);
    }

    public static EnumGender getRandom() {
        return new Random().nextBoolean() ? MALE : FEMALE;
    }

    public static EnumGender byName(String name) {
        Optional<EnumGender> gender = Arrays.stream(values()).filter((e) -> e.getStrName().equals(name)).findFirst();
        return gender.orElse(UNASSIGNED);
    }
}


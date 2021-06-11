package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@AllArgsConstructor
@Getter
public enum Gender {
    UNASSIGNED(0, "unassigned"),
    MALE(1, "male"),
    FEMALE(2, "female"),
    NEUTRAL(3, "neutral");

    int id;
    String strName;

    public static Gender byId(int id) {
        Optional<Gender> gender = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return gender.orElse(UNASSIGNED);
    }

    public static Gender getRandom() {
        return new Random().nextBoolean() ? MALE : FEMALE;
    }

    public static Gender byName(String name) {
        Optional<Gender> gender = Arrays.stream(values()).filter((e) -> e.getStrName().equals(name)).findFirst();
        return gender.orElse(UNASSIGNED);
    }
}


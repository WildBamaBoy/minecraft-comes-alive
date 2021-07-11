package mca.enums;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Gender {
    UNASSIGNED,
    MALE,
    FEMALE,
    NEUTRAL;

    private static final Random RNG = new Random();
    private static final Gender[] VALUES = values();
    private static final Map<String, Gender> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(Gender::name, Function.identity()));

    public int getId() {
        return ordinal();
    }

    public String getStrName() {
        return name().toLowerCase();
    }

    public Gender binary() {
        return this == FEMALE ? FEMALE : MALE;
    }

    public static Gender byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }

    public static Gender getRandom() {
        return RNG.nextBoolean() ? MALE : FEMALE;
    }

    public static Gender byName(String name) {
        return REGISTRY.getOrDefault(name.toUpperCase(), UNASSIGNED);
    }
}


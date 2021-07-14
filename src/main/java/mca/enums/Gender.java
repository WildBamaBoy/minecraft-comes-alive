package mca.enums;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mca.core.minecraft.EntitiesMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Formatting;

public enum Gender {
    UNASSIGNED(Formatting.WHITE),
    MALE(Formatting.AQUA),
    FEMALE(Formatting.LIGHT_PURPLE), // TODO: Girls should be pink
    NEUTRAL(Formatting.WHITE);

    private static final Random RNG = new Random();
    private static final Gender[] VALUES = values();
    private static final Map<String, Gender> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(Gender::name, Function.identity()));

    private final Formatting color;

    Gender(Formatting color) {
        this.color = color;
    }

    public EntityType<VillagerEntityMCA> getVillagerType() {
        return this == FEMALE ? EntitiesMCA.FEMALE_VILLAGER : EntitiesMCA.MALE_VILLAGER;
    }

    public Formatting getColor() {
        return color;
    }

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


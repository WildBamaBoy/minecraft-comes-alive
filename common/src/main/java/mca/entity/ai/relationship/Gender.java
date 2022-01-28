package mca.entity.ai.relationship;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mca.entity.EntitiesMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ZombieVillagerEntityMCA;
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

    public EntityType<ZombieVillagerEntityMCA> getZombieType() {
        return this == FEMALE ? EntitiesMCA.FEMALE_ZOMBIE_VILLAGER : EntitiesMCA.MALE_ZOMBIE_VILLAGER;
    }

    public Formatting getColor() {
        return color;
    }

    public int getId() {
        return ordinal();
    }

    public String getStrName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public boolean isNonBinary() {
        return this == NEUTRAL || this == UNASSIGNED;
    }

    public Stream<Gender> getTransients() {
        return isNonBinary() ? Stream.of(MALE, FEMALE) : Stream.of(this);
    }

    public Gender binary() {
        return this == FEMALE ? FEMALE : MALE;
    }

    public Gender opposite() {
        return this == FEMALE ? MALE : FEMALE;
    }

    /**
     * Checks whether this gender is attracted to another.
     */
    public boolean isAttractedTo(Gender other) {
        return other == UNASSIGNED || this == NEUTRAL || other != this;
    }

    /**
     * Checks whether both genders are mutually attracted to each other.
     */
    public boolean isMutuallyAttracted(Gender other) {
        return isAttractedTo(other) && other.isAttractedTo(this);
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
        return REGISTRY.getOrDefault(name.toUpperCase(Locale.ENGLISH), UNASSIGNED);
    }
}


package mca.client.gui;

import mca.entity.Relationship;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Constraint implements Relationship.Predicate {
    NOT_FAMILY("notfamily", Relationship.IS_FAMILY.negate()),
    FAMILY("family", Relationship.IS_FAMILY),
    ADULTS("adults", (villager, player) -> !villager.isBaby()),
    SPOUSE("spouse", Relationship.IS_MARRIED),
    NOT_SPOUSE("notspouse", Relationship.IS_MARRIED.negate()),
    /**
     * Internal.
     *
     * Used to hide a button when any of its other constraints fail
     */
    HIDE_ON_FAIL("hideonfail", (villager, player) -> true),
    NOT_YOUR_KIDS("notyourkids", Relationship.IS_PARENT);

    public static final Map<String, Constraint> REGISTRY = Stream.of(values()).collect(Collectors.toMap(a -> a.id, Function.identity()));

    private final String id;
    private final Relationship.Predicate check;

    Constraint(String id, Relationship.Predicate check) {
        this.id = id;
        this.check = check;
    }

    @Override
    public boolean test(VillagerEntityMCA t, UUID u) {
        return check.test(t, u);
    }

    public static Set<Constraint> all() {
        return new HashSet<>(REGISTRY.values());
    }

    public static Set<Constraint> allMatching(VillagerEntityMCA villager, Entity player) {
        return Stream.of(values()).filter(c -> c.test(villager, player)).collect(Collectors.toSet());
    }

    public static Stream<Constraint> fromStringList(String constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(constraints.split("\\|"))
                .map(REGISTRY::get)
                .filter(Objects::nonNull);
    }
}


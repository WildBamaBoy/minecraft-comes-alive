package mca.enums;

import mca.entity.Relationship;
import mca.entity.VillagerEntityMCA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

public enum Constraint implements Relationship.Predicate {
    NOT_FAMILY("notfamily", Relationship.IS_FAMILY),
    FAMILY("family", Relationship.IS_FAMILY.negate()),
    ADULTS("adults", (villager, player) -> villager.isBaby()),
    SPOUSE("spouse", Relationship.IS_MARRIED.negate()),
    NOT_SPOUSE("notspouse", Relationship.IS_MARRIED),
    HIDE_ON_FAIL("hideonfail", (villager, player) -> false), //internal
    NOT_YOUR_KIDS("notyourkids", Relationship.IS_PARENT);

    private static final Map<String, Constraint> REGISTRY = Stream.of(values()).collect(Collectors.toMap(a -> a.id, Function.identity()));

    private final String id;
    //* Returns true if it should not show the button
    private final Relationship.Predicate check;

    Constraint(String id, Relationship.Predicate check) {
        this.id = id;
        this.check = check;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean test(VillagerEntityMCA t, UUID u) {
        return check.test(t, u);
    }

    public static List<Constraint> fromStringList(String constraints) {
        List<Constraint> list = new ArrayList<>();

        if (constraints != null && !constraints.isEmpty()) {
            String[] splitConstraints = constraints.split("\\|");

            for (String s : splitConstraints) {
                Constraint constraint = byValue(s);
                if (s != null) {
                    list.add(constraint);
                }
            }
        }

        return list;
    }

    @Nullable
    public static Constraint byValue(String value) {
        return REGISTRY.get(value);
    }

}


package mca.client.gui;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.Rank;
import mca.entity.ai.Relationship;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.VillagerProfession;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Constraint implements BiPredicate<VillagerEntityMCA, Entity> {
    FAMILY("family", Relationship.IS_FAMILY),
    NOT_FAMILY("!family", Relationship.IS_FAMILY.negate()),

    ADULT("adult", (villager, player) -> !villager.isBaby()),
    NOT_ADULT("!adult", (villager, player) -> villager.isBaby()),

    SPOUSE("spouse", Relationship.IS_MARRIED),
    NOT_SPOUSE("!spouse", Relationship.IS_MARRIED.negate()),

    KIDS("kids", Relationship.IS_PARENT),
    NOT_KIDS("!kids", Relationship.IS_PARENT.negate()),

    CLERIC("cleric", (villager, player) -> villager.getProfession() == VillagerProfession.CLERIC),
    NOT_CLERIC("!cleric", (villager, player) -> villager.getProfession() != VillagerProfession.CLERIC),

    OUTLAWED("outlawed", (villager, player) -> villager.getProfession() == ProfessionsMCA.OUTLAW),
    NOT_OUTLAWED("!outlawed", (villager, player) -> villager.getProfession() == ProfessionsMCA.OUTLAW),

    PEASANT("peasant", (villager, player) -> {
        return player instanceof PlayerEntity && villager.getResidency().getHomeVillage().filter(village -> {
            return village.getRank((PlayerEntity)player).getReputation() >= Rank.PEASANT.getReputation();
        }).isPresent();
    }),
    NOT_PEASANT("!peasant", (villager, player) -> {
        return !(player instanceof PlayerEntity) || !villager.getResidency().getHomeVillage().filter(village -> {
            return village.getRank((PlayerEntity)player).getReputation() >= Rank.PEASANT.getReputation();
        }).isPresent();
    });

    public static final Map<String, Constraint> REGISTRY = Stream.of(values()).collect(Collectors.toMap(a -> a.id, Function.identity()));

    private final String id;
    private final BiPredicate<VillagerEntityMCA, Entity> check;

    Constraint(String id, BiPredicate<VillagerEntityMCA, Entity> check) {
        this.id = id;
        this.check = check;
    }

    @Override
    public boolean test(VillagerEntityMCA t, Entity u) {
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
        return Stream.of(constraints.split("\\,"))
                .map(REGISTRY::get)
                .filter(Objects::nonNull);
    }
}


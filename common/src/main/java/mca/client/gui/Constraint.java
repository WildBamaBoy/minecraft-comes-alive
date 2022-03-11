package mca.client.gui;

import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.MoveState;
import mca.entity.ai.ProfessionsMCA;
import mca.resources.Rank;
import mca.entity.ai.Relationship;
import mca.entity.ai.relationship.AgeState;
import mca.resources.Tasks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Constraint implements BiPredicate<VillagerLike<?>, Entity> {
    FAMILY("family", Relationship.IS_FAMILY.asConstraint()),
    NOT_FAMILY("!family", Relationship.IS_FAMILY.negate().asConstraint()),

    BABY("baby", (villager, player) -> villager.getAgeState() == AgeState.BABY),
    NOT_BABY("!baby", (villager, player) -> villager.getAgeState() != AgeState.BABY),

    TEEN("teen", (villager, player) -> villager.getAgeState() == AgeState.TEEN),
    NOT_TEEN("!teen", (villager, player) -> villager.getAgeState() != AgeState.TEEN),

    ADULT("adult", (villager, player) -> villager.getAgeState() == AgeState.ADULT),
    NOT_ADULT("!adult", (villager, player) -> villager.getAgeState() != AgeState.ADULT),

    SPOUSE("spouse", Relationship.IS_MARRIED.asConstraint()),
    NOT_SPOUSE("!spouse", Relationship.IS_MARRIED.negate().asConstraint()),

    KIDS("kids", Relationship.IS_PARENT.asConstraint()),
    NOT_KIDS("!kids", Relationship.IS_PARENT.negate().asConstraint()),

    CLERIC("cleric", (villager, player) -> villager.getVillagerData().getProfession() == VillagerProfession.CLERIC),
    NOT_CLERIC("!cleric", (villager, player) -> villager.getVillagerData().getProfession() != VillagerProfession.CLERIC),

    OUTLAWED("outlawed", (villager, player) -> villager.getVillagerData().getProfession() == ProfessionsMCA.OUTLAW),
    NOT_OUTLAWED("!outlawed", (villager, player) -> villager.getVillagerData().getProfession() != ProfessionsMCA.OUTLAW),

    TRADER("trader", (villager, player) -> !ProfessionsMCA.canNotTrade.contains(villager.getVillagerData().getProfession())),
    NOT_TRADER("!trader", (villager, player) -> ProfessionsMCA.canNotTrade.contains(villager.getVillagerData().getProfession())),

    PEASANT("peasant", (villager, player) -> isRankAtLeast(villager, player, Rank.PEASANT)),
    NOT_PEASANT("!peasant", (villager, player) -> !isRankAtLeast(villager, player, Rank.PEASANT)),

    NOBLE("noble", (villager, player) -> isRankAtLeast(villager, player, Rank.NOBLE)),
    NOT_NOBLE("!noble", (villager, player) -> !isRankAtLeast(villager, player, Rank.NOBLE)),

    MAYOR("mayor", (villager, player) -> isRankAtLeast(villager, player, Rank.MAYOR)),
    NOT_MAYOR("!mayor", (villager, player) -> !isRankAtLeast(villager, player, Rank.MAYOR)),

    KING("king", (villager, player) -> isRankAtLeast(villager, player, Rank.KING)),
    NOT_KING("!king", (villager, player) -> !isRankAtLeast(villager, player, Rank.KING)),

    ORPHAN("orphan", Relationship.IS_ORPHAN.asConstraint()),
    NOT_ORPHAN("!orphan", Relationship.IS_ORPHAN.negate().asConstraint()),

    FOLLOWING("following", (villager, player) -> villager.getVillagerBrain().getMoveState() == MoveState.FOLLOW),
    NOT_FOLLOWING("!following", (villager, player) -> villager.getVillagerBrain().getMoveState() != MoveState.FOLLOW),

    STAYING("staying", (villager, player) -> villager.getVillagerBrain().getMoveState() == MoveState.STAY),
    NOT_STAYING("!staying", (villager, player) -> villager.getVillagerBrain().getMoveState() != MoveState.STAY);

    private static boolean isRankAtLeast(VillagerLike<?> villager, Entity player, Rank rank) {
        return player instanceof PlayerEntity && villager instanceof VillagerEntityMCA && ((VillagerEntityMCA)villager).getResidency().getHomeVillage()
                .filter(village -> Tasks.getRank(village, (ServerPlayerEntity)player).isAtLeast(rank)).isPresent();
    }

    public static final Map<String, Constraint> REGISTRY = Stream.of(values()).collect(Collectors.toMap(a -> a.id, Function.identity()));

    private final String id;
    private final BiPredicate<VillagerLike<?>, Entity> check;

    Constraint(String id, BiPredicate<VillagerLike<?>, Entity> check) {
        this.id = id;
        this.check = check;
    }

    @Override
    public boolean test(VillagerLike<?> t, Entity u) {
        return check.test(t, u);
    }

    public static Set<Constraint> all() {
        return new HashSet<>(REGISTRY.values());
    }

    public static Set<Constraint> allMatching(VillagerLike<?> villager, Entity player) {
        return Stream.of(values()).filter(c -> c.test(villager, player)).collect(Collectors.toSet());
    }

    public static List<Constraint> fromStringList(String constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return new ArrayList<>();
        }
        return Stream.of(constraints.split("\\,"))
                .map(REGISTRY::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}


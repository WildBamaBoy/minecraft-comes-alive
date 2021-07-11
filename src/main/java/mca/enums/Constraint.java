package mca.enums;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;

public enum Constraint {
    NOT_FAMILY("notfamily", (villager, player) -> villager.getFamilyTree().isRelative(villager.getUuid(), player.getUuid()) || villager.isMarriedTo(player.getUuid())),
    FAMILY("family", (villager, player) -> !(villager.getFamilyTree().isRelative(villager.getUuid(), player.getUuid()) || villager.isMarriedTo(player.getUuid()))),
    ADULTS("adults", (villager, player) -> villager.isBaby()),
    SPOUSE("spouse", (villager, player) -> !villager.isMarriedTo(player.getUuid())),
    NOT_SPOUSE("notspouse", (villager, player) -> villager.isMarriedTo(player.getUuid())),
    HIDE_ON_FAIL("hideonfail", (villager, player) -> false), //internal
    NOT_YOUR_KIDS("notyourkids", (villager, player) -> villager.getFamilyTree().isParent(villager.getUuid(), player.getUuid()));

    private final String id;
    //* Returns true if it should not show the button
    private final BiPredicate<VillagerEntityMCA, PlayerEntity> check;

    Constraint(String id, BiPredicate<VillagerEntityMCA, PlayerEntity> check) {
        this.id = id;
        this.check = check;
    }

    public String getId() {
        return id;
    }

    public BiPredicate<VillagerEntityMCA, PlayerEntity> getCheck() {
        return check;
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
        return Arrays.stream(values())
                .filter((e) -> e.id.equals(value))
                .findFirst()
                .orElse(null);
    }
}


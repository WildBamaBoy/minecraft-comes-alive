package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@AllArgsConstructor
@Getter
public enum Constraint {
    NOT_FAMILY("notfamily", (villager, player) -> villager.playerIsParent(player) || villager.isMarriedTo(player.getUUID())),
    FAMILY("family", (villager, player) -> !(villager.playerIsParent(player) || villager.isMarriedTo(player.getUUID()))),
    ADULTS("adults", (villager, player) -> villager.isBaby()),
    SPOUSE("spouse", (villager, player) -> !villager.isMarriedTo(player.getUUID())),
    NOT_SPOUSE("notspouse", (villager, player) -> villager.isMarriedTo(player.getUUID())),
    HIDE_ON_FAIL("hideonfail", (villager, player) -> false),//internal
    NOT_YOUR_KIDS("notyourkids", VillagerEntityMCA::playerIsParent);

    String id;
    //* Returns true if it should not show the button
    BiPredicate<VillagerEntityMCA, PlayerEntity> check;

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

    public static Constraint byValue(String value) {
        Optional<Constraint> state = Arrays.stream(values()).filter((e) -> e.id.equals(value)).findFirst();
        return state.orElse(null);
    }

}


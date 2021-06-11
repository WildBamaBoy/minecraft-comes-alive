package mca.items;

import mca.entity.VillagerEntityMCA;
import mca.enums.AgeState;
import mca.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.Comparator;

public class MatchmakersRingItem extends Item implements SpecialCaseGift {
    public MatchmakersRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean handle(PlayerEntity player, VillagerEntityMCA villager) {
        // ensure two rings are in the inventory
        if (player.getMainHandItem().getCount() < 2) {
            villager.say(player, "interaction.matchmaker.fail.needtwo");
            return false;
        }

        // ensure our target isn't married already or young
        if (villager.isMarried() || villager.getAgeState() != AgeState.ADULT) {
            villager.say(player, "interaction.matchmaker.fail.married");
            return false;
        }

        // look for partner
        java.util.Optional<VillagerEntityMCA> target = WorldUtils.getCloseEntities(villager.level, villager, 3.0).stream()
                .filter(v -> v != villager && v instanceof VillagerEntityMCA)
                .map(v -> (VillagerEntityMCA) v)
                .filter(v -> !v.isBaby() && !v.isMarried())
                .min(Comparator.comparingDouble(villager::distanceTo));

        // ensure we found a nearby villager
        if (!target.isPresent()) {
            villager.say(player, "interaction.matchmaker.fail.novillagers");
            return false;
        }

        // setup the marriage by assigning spouse UUIDs
        VillagerEntityMCA spouse = target.get();
        villager.marry(spouse);
        spouse.marry(villager);

        // show a reaction
        player.level.broadcastEntityEvent(villager, (byte) 12);

        // remove the rings for survival mode
        if (!player.isCreative())
            player.getMainHandItem().shrink(1);
        return true;
    }
}

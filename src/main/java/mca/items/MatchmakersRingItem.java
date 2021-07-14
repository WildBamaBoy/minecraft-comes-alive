package mca.items;

import mca.entity.VillagerEntityMCA;
import mca.enums.AgeState;
import mca.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.Comparator;
import java.util.Optional;

public class MatchmakersRingItem extends Item implements SpecialCaseGift {
    public MatchmakersRingItem(Settings properties) {
        super(properties);
    }

    @Override
    public boolean handle(PlayerEntity player, VillagerEntityMCA villager) {
        // ensure two rings are in the inventory
        if (player.getMainHandStack().getCount() < 2) {
            villager.sendChatMessage(player, "interaction.matchmaker.fail.needtwo");
            return false;
        }

        // ensure our target isn't married already or young
        if (villager.getRelationships().isMarried() || villager.getAgeState() != AgeState.ADULT) {
            villager.sendChatMessage(player, "interaction.matchmaker.fail.married");
            return false;
        }

        // look for partner
        Optional<VillagerEntityMCA> target = WorldUtils.getCloseEntities(villager.world, villager, 3.0).stream()
                .filter(v -> v != villager && v instanceof VillagerEntityMCA)
                .map(v -> (VillagerEntityMCA) v)
                .filter(v -> !v.isBaby() && !v.getRelationships().isMarried())
                .min(Comparator.comparingDouble(villager::distanceTo));

        // ensure we found a nearby villager
        if (!target.isPresent()) {
            villager.sendChatMessage(player, "interaction.matchmaker.fail.novillagers");
            return false;
        }

        // setup the marriage by assigning spouse UUIDs
        VillagerEntityMCA spouse = target.get();
        villager.getRelationships().marry(spouse);
        spouse.getRelationships().marry(villager);

        // show a reaction
        player.world.sendEntityStatus(villager, (byte) 12);

        // remove the rings for survival mode
        if (!player.isCreative()) {
            player.getMainHandStack().decrement(1);
        }

        return true;
    }
}

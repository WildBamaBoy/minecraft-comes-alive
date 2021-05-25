package mca.items;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;

public class ItemMatchmakersRing extends ItemSpecialCaseGift {
    public ItemMatchmakersRing(Properties properties) {
        super(properties);
    }

    @Override
    public boolean handle(PlayerEntity player, EntityVillagerMCA villager) {
        // ensure two rings are in the inventory
        if (player.getMainHandItem().getCount() < 2) {
            villager.say(player, "interaction.matchmaker.fail.needtwo");
            return false;
        }

        // ensure our target isn't married already
        if (villager.isMarried()) {
            villager.say(player, "interaction.matchmaker.fail.married");
            return false;
        }

        // look for partner
        java.util.Optional<EntityVillagerMCA> target = villager.world.getCloseEntities(villager, 3.0).stream()
                .filter(v -> v != villager && v instanceof EntityVillagerMCA)
                .map(v -> (EntityVillagerMCA) v)
                .filter(v -> !v.isBaby() && !v.isMarried())
                .min(Comparator.comparingDouble(villager::distanceTo));

        // ensure we found a nearby villager
        if (!target.isPresent()) {
            villager.say(player, "interaction.matchmaker.fail.novillagers");
            return false;
        }

        // setup the marriage by assigning spouse UUIDs
        EntityVillagerMCA spouse = target.get();
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

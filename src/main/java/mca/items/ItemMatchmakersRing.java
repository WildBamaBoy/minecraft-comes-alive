package mca.items;

import com.google.common.base.Optional;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class ItemMatchmakersRing extends ItemSpecialCaseGift {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        // ensure two rings are in the inventory
        if (player.inventory.getStackInSlot(player.inventory.currentItem).getCount() < 2) {
            villager.say(player, "interaction.matchmaker.fail.needtwo");
            return false;
        }

        // ensure our target isn't married already
        if (villager.isMarried()) {
            villager.say(player, "interaction.matchmaker.fail.married");
            return false;
        }

        List<EntityVillagerMCA> villagers = villager.world.getEntities(EntityVillagerMCA.class, v -> !v.isMarried() && !v.isChild() && v.getDistance(villager) < 3.0D);
        java.util.Optional<EntityVillagerMCA> target = villagers.stream().min(Comparator.comparingDouble(villager::getDistance));

        // ensure we found a nearby villager
        if (!target.isPresent()) {
            villager.say(player, "interaction.matchmaker.fail.novillagers");
            return false;
        }

        // setup the marriage by assigning spouse UUIDs
        villager.set(EntityVillagerMCA.SPOUSE_UUID, Optional.of(target.get().getUniqueID()));
        target.get().set(EntityVillagerMCA.SPOUSE_UUID, Optional.of(villager.getUniqueID()));

        // remove the rings for survival mode
        if (!player.isCreative()) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
        }

        return true;
    }
}

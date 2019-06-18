package mca.items;

import com.google.common.base.Optional;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMarriageState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;

import java.util.Comparator;
import java.util.List;

public class ItemMatchmakersRing extends ItemSpecialCaseGift {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        // ensure two rings are in the inventory
        if (player.inventory.getStackInSlot(player.inventory.currentItem).getCount() < 2) {
            villager.say(Optional.of(player), "interaction.matchmaker.fail.needtwo");
            return false;
        }

        // ensure our target isn't married already
        if (villager.isMarried()) {
            villager.say(Optional.of(player), "interaction.matchmaker.fail.married");
            return false;
        }

        List<EntityVillagerMCA> villagers = villager.world.getEntities(EntityVillagerMCA.class, v -> v != null && !v.isMarried() && !v.isChild() && v.getDistance(villager) < 3.0D && v != villager);
        java.util.Optional<EntityVillagerMCA> target = villagers.stream().min(Comparator.comparingDouble(villager::getDistance));

        // ensure we found a nearby villager
        if (!target.isPresent()) {
            villager.say(Optional.of(player), "interaction.matchmaker.fail.novillagers");
            return false;
        }

        // setup the marriage by assigning spouse UUIDs
        EntityVillagerMCA spouse = target.get();
        villager.set(EntityVillagerMCA.SPOUSE_UUID, Optional.of(target.get().getUniqueID()));
        villager.set(EntityVillagerMCA.MARRIAGE_STATE, EnumMarriageState.MARRIED.getId());
        villager.set(EntityVillagerMCA.SPOUSE_NAME, spouse.get(EntityVillagerMCA.VILLAGER_NAME));
        spouse.set(EntityVillagerMCA.SPOUSE_UUID, Optional.of(villager.getUniqueID()));
        spouse.set(EntityVillagerMCA.MARRIAGE_STATE, EnumMarriageState.MARRIED.getId());
        spouse.set(EntityVillagerMCA.SPOUSE_NAME, villager.get(EntityVillagerMCA.VILLAGER_NAME));

        // spawn hearts to show something happened
        villager.spawnParticles(EnumParticleTypes.HEART);
        target.get().spawnParticles(EnumParticleTypes.HEART);

        // remove the rings for survival mode
        if (!player.isCreative()) player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
        return true;
    }
}

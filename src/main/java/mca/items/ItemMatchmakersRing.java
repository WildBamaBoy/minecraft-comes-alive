package mca.items;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import com.google.common.base.Optional;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMarriageState;
import cobalt.minecraft.entity.player.CPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EnumParticleTypes;

import java.util.Comparator;
import java.util.List;

public class ItemMatchmakersRing extends ItemSpecialCaseGift {
    @Override
    public ActionResult<ItemStack> handleRightClick(CWorld worldIn, CPlayer player, CEnumHand hand) {
        // ensure two rings are in the inventory
        if (player.getHeldItem(hand).getCount() < 2) {
            villager.say(player, "interaction.matchmaker.fail.needtwo");
        }

        // ensure our target isn't married already
        if (villager.isMarried()) {
            villager.say(player, "interaction.matchmaker.fail.married");
        }

        List<EntityVillagerMCA> villagers = villager.world.getEntities(EntityVillagerMCA.class, v -> v != null && !v.isMarried() && !v.isChild() && v.getDistance(villager) < 3.0D && v != villager);
        java.util.Optional<EntityVillagerMCA> target = villagers.stream().min(Comparator.comparingDouble(villager::getDistance));

        // ensure we found a nearby villager
        if (!target.isPresent()) {
            villager.say(player, "interaction.matchmaker.fail.novillagers");
        }

        // setup the marriage by assigning spouse UUIDs
        EntityVillagerMCA spouse = target.get();
        villager.marry(spouse);
        spouse.marry(villager);

        // spawn hearts to show something happened
        villager.spawnParticles(EnumParticleTypes.HEART);
        target.get().spawnParticles(EnumParticleTypes.HEART);

        // remove the rings for survival mode
        if (!player.isCreative()) player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
    }

    @Override
    public ActionResultType handleUseOnBlock(CItemUseContext context) {
        return null;
    }
}

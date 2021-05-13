package mca.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemMatchmakersRing extends ItemSpecialCaseGift {
    public ItemMatchmakersRing(Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//        // ensure two rings are in the inventory
//        if (player.getHeldItem(hand).getCount() < 2) {
//            villager.say(player, "interaction.matchmaker.fail.needtwo");
//        }
//
//        // ensure our target isn't married already
//        if (villager.isMarried()) {
//            villager.say(player, "interaction.matchmaker.fail.married");
//        }
//
//        List<EntityVillagerMCA> villagers = villager.world.getEntities(EntityVillagerMCA.class, v -> v != null && !v.isMarried() && !v.isBaby() && v.getDistance(villager) < 3.0D && v != villager);
//        java.util.Optional<EntityVillagerMCA> target = villagers.stream().min(Comparator.comparingDouble(villager::getDistance));
//
//        // ensure we found a nearby villager
//        if (!target.isPresent()) {
//            villager.say(player, "interaction.matchmaker.fail.novillagers");
//        }
//
//        // setup the marriage by assigning spouse UUIDs
//        EntityVillagerMCA spouse = target.get();
//        villager.marry(spouse);
//        spouse.marry(villager);
//
//        // spawn hearts to show something happened
////            villager.spawnParticles(EnumParticleTypes.HEART);
////            target.get().spawnParticles(EnumParticleTypes.HEART);
//
//        // remove the rings for survival mode
//        if (!player.isCreative())
//            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
        return null;
    }
}

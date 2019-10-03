package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;

public class EntityAIFishing extends AbstractEntityAIChore {
    private BlockPos targetWater;
    private boolean hasCastRod;
    private int ticks;

    public EntityAIFishing(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.FISH;
    }

    public void updateTask() {
        super.updateTask();

        if (!villager.inventory.contains(ItemFishingRod.class)) {
            villager.say(getAssigningPlayer(), "chore.fishing.norod");
            villager.stopChore();
            return;
        }

        if (targetWater == null) {
            List<BlockPos> nearbyStaticLiquid = Util.getNearbyBlocks(villager.getPos(), villager.world, BlockStaticLiquid.class, 12, 3);
            targetWater = nearbyStaticLiquid.stream()
                    .filter((p) -> villager.world.getBlockState(p).getBlock() == Blocks.WATER)
                    .min(Comparator.comparingDouble(villager::getDistanceSq)).orElse(null);
        } else if (villager.getDistanceSq(targetWater) > 5.0D) villager.getNavigator().setPath(villager.getNavigator().getPathToPos(targetWater), 0.8D);
        else if (villager.getDistanceSq(targetWater) < 5.0D) {
            villager.getNavigator().clearPath();

            if (!hasCastRod) {
                villager.swingArm(EnumHand.MAIN_HAND);
                hasCastRod = true;
            }

            ticks++;

            if (ticks >= villager.world.rand.nextInt(200) + 200) {
                if (villager.world.rand.nextFloat() >= 0.35F) {
                    int typesSize = ItemFishFood.FishType.values().length;
                    ItemFishFood.FishType type = ItemFishFood.FishType.values()[villager.world.rand.nextInt(typesSize)];
                    ItemStack stack = new ItemStack(Items.FISH, 1, type.getMetadata());

                    villager.swingArm(EnumHand.MAIN_HAND);
                    villager.inventory.addItem(stack);
                    villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(2, villager);
                }
                ticks = 0;
            }
        }
    }
}
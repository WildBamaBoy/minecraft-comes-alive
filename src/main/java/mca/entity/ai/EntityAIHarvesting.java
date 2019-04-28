package mca.entity.ai;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class EntityAIHarvesting extends AbstractEntityAIChore {
    private BlockPos target;

    public EntityAIHarvesting(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(4);
    }

    public boolean shouldExecute() {
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.HARVEST;
    }

    public void updateTask() {
        if (target == null) {
            List<BlockPos> nearbyCrops = Util.getNearbyBlocks(villager.getPos(), villager.world, BlockCrops.class, 8, 3);
            List<BlockPos> harvestable = new ArrayList<>();
            for (BlockPos pos : nearbyCrops) {
                IBlockState state = villager.world.getBlockState(pos);
                BlockCrops crop = (BlockCrops) state.getBlock();

                if (crop.isMaxAge(state)) {
                    harvestable.add(pos);
                }
            }

            target = Util.getNearestPoint(villager.getPos(), harvestable);
        } else {
            double distanceTo = Math.sqrt(villager.getDistanceSq(target));
            if (distanceTo >= 2.0D) {
                villager.getNavigator().setPath(villager.getNavigator().getPathToPos(target), 0.5D);
            } else {
                IBlockState state = villager.world.getBlockState(target);

                if (state.getBlock() instanceof BlockCrops) {
                    BlockCrops crop = (BlockCrops) state.getBlock();
                    NonNullList<ItemStack> drops = NonNullList.create();
                    crop.getDrops(drops, villager.world, target, state, 0);
                    for (ItemStack stack : drops) {
                        villager.inventory.addItem(stack);
                    }

                    villager.swingArm(EnumHand.MAIN_HAND);

                    try {
                        IProperty<Integer> property = (IProperty<Integer>) crop.getBlockState().getProperty("age");
                        villager.world.setBlockState(target, state.withProperty(property, 0));
                    } catch (Exception e) { //age property may have some issues on certain mods, if it errors just set to air
                        MCA.getLog().warn("Error resetting crop age at " + target.toString() + "! Setting to air.");
                        villager.world.setBlockToAir(target);
                    }
                    target = null;
                } else { //Target is no longer a crop block, so we null it out and get a different target
                    target = null;
                }
            }
        }
    }
}
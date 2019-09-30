package mca.entity.ai;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.util.Util;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.block.BlockBed.OCCUPIED;

public class EntityAISleeping extends AbstractEntityAIChore {
    public EntityAISleeping(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.get(EntityVillagerMCA.BED_POS) == BlockPos.ORIGIN) {
            return false; //can be disabled to allow every villager to sleep
        }

        long time = villager.world.getWorldTime() % 24000L;
        if (time > 13000 && time < 23000) {
            return true;
        } else {
            //wake up if still sleeping
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }
    }

    public boolean shouldContinueExecuting() {
        return !villager.getNavigator().noPath() || (shouldExecute() && villager.isSleeping());
    }

    public void startExecuting() {
        if (villager.get(EntityVillagerMCA.BED_POS) == BlockPos.ORIGIN || villager.getDistanceSq(villager.get(EntityVillagerMCA.BED_POS)) < 4.0) {
            //search for the nearest bed, might be a different than before
            List<BlockPos> nearbyBeds = Util.getNearbyBlocks(villager.getPos(), villager.world, BlockBed.class, 8, 8);
            List<BlockPos> valid = new ArrayList<>();
            for (BlockPos pos : nearbyBeds) {
                IBlockState state = villager.world.getBlockState(pos);
                if (!state.getValue(OCCUPIED)) {
                    valid.add(pos);
                }
            }

            BlockPos pos = Util.getNearestPoint(villager.getPos(), valid);

            if (pos == null) {
                //no bed found, let's forget about the remembered bed
                //TODO: notify the player
                MCA.getLog().info(villager.getName() + " lost the bed");
                villager.set(EntityVillagerMCA.BED_POS, BlockPos.ORIGIN);
            } else {
                MCA.getLog().info(villager.getName() + " sleeps now");
                villager.set(EntityVillagerMCA.BED_POS, pos);
                villager.startSleeping();
            }
        } else {
            MCA.getLog().info(villager.getName() + " is going to bed");
            villager.moveTowardsBlock(villager.get(EntityVillagerMCA.BED_POS));
        }
    }

    public void resetTask() {
        if (villager.isSleeping()) {
            MCA.getLog().info("Villager wakes up");
            villager.stopSleeping();
        }
    }

    public void updateTask() {
        //villager.getLookHelper().setLookPosition(villager.posX, villager.posY - 10.0f, villager.posZ, (float)villager.getHorizontalFaceSpeed(), (float)villager.getVerticalFaceSpeed());
        //TODO: implement non-nightmare-style head movements while sleeping
        //TODO: check if bed has been destroyed
    }
}

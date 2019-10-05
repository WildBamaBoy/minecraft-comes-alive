package mca.entity.ai;

import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.util.math.BlockPos;

public class EntityAISleeping extends AbstractEntityAIChore {
    private int failed = 0;

    public EntityAISleeping(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        //let the avoid tasks work
        if (villager.getHealth() < villager.getMaxHealth()) {
            return false;
        }

        if (villager.ticksExisted - failed < 1200) {
            //wake up if still sleeping
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }

        long time = villager.world.getWorldTime() % 24000L;
        if (villager.get(EntityVillagerMCA.BED_POS) == BlockPos.ORIGIN && time < 16000) { //at tick 18000 villager without bed are allowed to automatically choose one
            //wake up if still sleeping
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }

        //if guards detect enemies they won't sleep
        if (villager.getProfessionForge() == ProfessionsMCA.guard && villager.getAttackTarget() != null) {
            //wake up, this is a emergency!
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }

        if (time > (villager.getProfessionForge() == ProfessionsMCA.guard ? 14000 : 12000) && time < 23000) {
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
        return shouldExecute() && (!villager.getNavigator().noPath() || villager.isSleeping());
    }

    public void startExecuting() {
        if (villager.get(EntityVillagerMCA.BED_POS) == BlockPos.ORIGIN || villager.getDistanceSq(villager.get(EntityVillagerMCA.BED_POS)) < 4.0) {
            //search for the nearest bed, might be a different than before
            BlockPos pos = villager.searchBed();

            if (pos == null) {
                //no bed found, let's forget about the remembered bed
                if (villager.get(EntityVillagerMCA.BED_POS) != BlockPos.ORIGIN) {
                    //TODO: notify the player?
                    //MCA.getLog().info(villager.getName() + " lost the bed");
                    villager.set(EntityVillagerMCA.BED_POS, BlockPos.ORIGIN);
                } else {
                    //MCA.getLog().info(villager.getName() + " has no bed");
                }
                failed = villager.ticksExisted;
            } else {
                //MCA.getLog().info(villager.getName() + " sleeps now");
                villager.set(EntityVillagerMCA.BED_POS, pos);
                villager.startSleeping();
            }
        } else {
            //MCA.getLog().info(villager.getName() + " is going to bed");
            villager.moveTowardsBlock(villager.get(EntityVillagerMCA.BED_POS), 0.75);
        }
    }

    public void resetTask() {
        if (villager.isSleeping()) {
            //MCA.getLog().info(villager.getName() + " wakes up");
            villager.stopSleeping();
        }
    }

    public void updateTask() {
        if (villager.isSleeping()) {
            villager.setRotationYawHead(0.0f);
            villager.rotationYaw = 0.0f;
        }
    }
}

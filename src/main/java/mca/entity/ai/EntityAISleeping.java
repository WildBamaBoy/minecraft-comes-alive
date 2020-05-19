package mca.entity.ai;

import static net.minecraft.block.BlockBed.OCCUPIED;
import static net.minecraft.block.BlockBed.PART;

import java.util.ArrayList;
import java.util.List;

import mca.api.objects.Pos;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.util.Util;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityAISleeping extends AbstractEntityAIChore {
    public EntityAISleeping(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        //let the avoid tasks work
        if (villager.getHealth() < villager.getMaxHealth()) {
            return false;
        }

        long time = villager.world.getVanillaWorld().getWorldTime() % 24000L;
        if (villager.get(EntityVillagerMCA.BED_POS) == BlockPos.ORIGIN && time < 16000) { //at tick 18000 villager without bed are allowed to automatically choose one
            //wake up if still sleeping
            if (isSleeping()) {
                stopSleeping();
            }
            return false;
        }

        //if guards detect enemies they won't sleep
        if (villager.getProfessionForge() == ProfessionsMCA.guard && villager.getAttackTarget() != null) {
            //wake up, this is a emergency!
            if (isSleeping()) {
                stopSleeping();
            }
            return false;
        }

        if (time > (villager.getProfessionForge() == ProfessionsMCA.guard ? 14000 : 12000) && time < 23000) {
            return true;
        } else {
            //wake up if still sleeping
            if (isSleeping()) {
                stopSleeping();
            }
            return false;
        }
    }

    public boolean shouldContinueExecuting() {
        return shouldExecute() && (!villager.getNavigator().noPath() || isSleeping());
    }

    public void startExecuting() {
    	BlockPos bedPos = villager.get(EntityVillagerMCA.BED_POS);
        if (bedPos == BlockPos.ORIGIN || villager.getDistanceSq(bedPos) < 4.0) {
            //search for the nearest bed, might be different than before
            Pos pos = EntityAISleeping.findAnyBed(villager);

            if (pos == null) {
                //no bed found, let's forget about the remembered bed
                if (bedPos != BlockPos.ORIGIN) {
                    //TODO: notify the player?
                    villager.set(EntityVillagerMCA.BED_POS, BlockPos.ORIGIN);
                }
            } else {
                villager.set(EntityVillagerMCA.BED_POS, pos.getBlockPos());
                startSleeping();
            }
        } else {
            moveTowardsBlock(bedPos, 0.75);
        }
    }

    public void resetTask() {
        if (isSleeping()) {
            stopSleeping();
        }
    }

    public void updateTask() {
    	updateSleeping();
    	
        if (isSleeping()) {
            villager.setRotationYawHead(0.0f);
            villager.rotationYaw = 0.0f;
        }
    }

    private void moveTowardsBlock(BlockPos target, double speed) {
        double range = villager.getNavigator().getPathSearchRange() - 6.0D;

        if (villager.getDistanceSq(target) > Math.pow(range, 2.0)) {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(villager, (int) range, 8, new Vec3d(target.getX(), target.getY(), target.getZ()));
            if (vec3d != null && !villager.getNavigator().setPath(villager.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z), speed)) {
            	villager.attemptTeleport(vec3d.x, vec3d.y, vec3d.z);
            }
        } else {
            if (!villager.getNavigator().setPath(villager.getNavigator().getPathToPos(target), speed)) {
            	villager.attemptTeleport(target.getX(), target.getY(), target.getZ());
            }
        }
    }

    //searches for the nearest bed
    public static Pos findAnyBed(EntityVillagerMCA villager) {
        List<Pos> nearbyBeds = Util.getNearbyBlocks(Util.wrapPos(villager), villager.world, BlockBed.class, 8, 8);
        List<Pos> valid = new ArrayList<>();
        for (Pos pos : nearbyBeds) {
            IBlockState state = villager.world.getBlockState(pos);
            if (!(state.getValue(OCCUPIED)) && state.getValue(PART) != BlockBed.EnumPartType.HEAD) {
                valid.add(pos);
            }
        }
        return Util.getNearestPoint(new Pos(villager.getPos()), valid);
    }

    private boolean isSleeping() {
        return villager.get(EntityVillagerMCA.SLEEPING);
    }

    private void updateSleeping() {
        if (isSleeping()) {
            BlockPos bedLocation = villager.get(EntityVillagerMCA.BED_POS);

            final IBlockState state = villager.world.getVanillaWorld().isBlockLoaded(bedLocation) ? villager.world.getVanillaWorld().getBlockState(bedLocation) : null;
            final boolean isBed = state != null && state.getBlock().isBed(state, villager.world.getVanillaWorld(), bedLocation, villager);

            if (isBed) {
                final EnumFacing enumfacing = state.getBlock() instanceof BlockHorizontal ? state.getValue(BlockHorizontal.FACING) : null;

                if (enumfacing != null) {
                    float f1 = 0.5F + (float) enumfacing.getFrontOffsetX() * 0.4F;
                    float f = 0.5F + (float) enumfacing.getFrontOffsetZ() * 0.4F;
                    this.setRenderOffsetForSleep(enumfacing);
                    villager.setPosition((double) ((float) bedLocation.getX() + f1), (double) ((float) bedLocation.getY() + 0.6875F), (double) ((float) bedLocation.getZ() + f));
                } else {
                	villager.setPosition((double) ((float) bedLocation.getX() + 0.5F), (double) ((float) bedLocation.getY() + 0.6875F), (double) ((float) bedLocation.getZ() + 0.5F));
                }

                villager.setSizePublic(0.2F, 0.2F);

                villager.motionX = 0.0D;
                villager.motionY = 0.0D;
                villager.motionZ = 0.0D;
            } else { //No bed
            	villager.set(EntityVillagerMCA.BED_POS, BlockPos.ORIGIN);
                stopSleeping();
            }
        } else {
        	villager.setSizePublic(0.6F, 1.8F);
        }
    }

    private void setRenderOffsetForSleep(EnumFacing bedDirection) {
    	villager.setRenderOffsetX(-1.0F * (float) bedDirection.getFrontOffsetX());
    	villager.setRenderOffsetZ(-1.0F * (float) bedDirection.getFrontOffsetZ());
    }

    private void startSleeping() {
        if (villager.isRiding()) {
        	villager.dismountRidingEntity();
        }

        villager.set(EntityVillagerMCA.SLEEPING, true);

        BlockPos bedLocation = villager.get(EntityVillagerMCA.BED_POS);
        IBlockState blockstate = villager.world.getVanillaWorld().getBlockState(bedLocation);
        if (blockstate.getBlock() == Blocks.BED) {
            blockstate.getBlock().setBedOccupied(villager.world.getVanillaWorld(), bedLocation, null, true);
        }
    }

    private void stopSleeping() {
        BlockPos bedLocation = villager.get(EntityVillagerMCA.BED_POS);
        if (bedLocation != BlockPos.ORIGIN) {
            IBlockState blockstate = villager.world.getVanillaWorld().getBlockState(bedLocation);

            if (blockstate.getBlock().isBed(blockstate, villager.world.getVanillaWorld(), bedLocation, villager)) {
                blockstate.getBlock().setBedOccupied(villager.world.getVanillaWorld(), bedLocation, null, false);
                BlockPos blockpos = blockstate.getBlock().getBedSpawnPosition(blockstate, villager.world.getVanillaWorld(), bedLocation, null);

                if (blockpos == null) {
                    blockpos = bedLocation.up();
                }

                villager.setPosition((double) ((float) blockpos.getX() + 0.5F), (double) ((float) blockpos.getY() + 0.1F), (double) ((float) blockpos.getZ() + 0.5F));
            }
        }

        villager.set(EntityVillagerMCA.SLEEPING, false);
    }
}

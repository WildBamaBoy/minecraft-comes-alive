package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FollowTask extends Task<VillagerEntityMCA> {

    public FollowTask() {
        super(ImmutableMap.of(
                MemoryModuleTypeMCA.PLAYER_FOLLOWING, MemoryModuleState.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return this.shouldRun(world, villager);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        PlayerEntity playerToFollow = villager.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).get();
        villager.getNavigation().startMovingTo(playerToFollow, villager.hasVehicle() ? 1.7D : 0.8D);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        PlayerEntity playerToFollow = villager.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).get();

        double distance = villager.squaredDistanceTo(playerToFollow);
        if (distance >= 4.0D && distance <= 100.0D) {
            villager.getNavigation().startMovingTo(playerToFollow, villager.hasVehicle() ? 1.7D : 0.8D);
        } else if (distance > 100.0D) {
            //teleportation when flying can kill the villager so we just let them walk on the surface.

            Vec3d teleportPos = Vec3d.ofBottomCenter(getGroundLevel(world, villager, playerToFollow.getBlockPos()).up());
            Entity teleportedEntity = getTeleportedEntity(villager);
            teleportedEntity.fallDistance = 0;
            villager.fallDistance = 0;
            teleportedEntity.requestTeleport(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ());
        } else {
            villager.getNavigation().stop();
        }
    }

    private static Entity getTeleportedEntity(Entity entity) {
        return entity.hasVehicle() ? entity.getVehicle() : entity;
    }

    private static BlockPos getGroundLevel(ServerWorld world, Entity entity, BlockPos pos) {
        BlockPos original = pos;
        while ((world.isAir(pos) || !world.isTopSolid(pos, entity)) && !world.getFluidState(pos).isEmpty()) {
            pos = pos.down();

            if (World.isOutOfBuildLimitVertically(pos)) {
                return original;
            }
        }

        return pos;
    }
}

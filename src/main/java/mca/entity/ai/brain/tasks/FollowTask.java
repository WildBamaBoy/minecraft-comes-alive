package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.minecraft.MemoryModuleTypeMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;

public class FollowTask extends Task<VillagerEntityMCA> {

    public FollowTask() {
        super(ImmutableMap.of(MemoryModuleTypeMCA.PLAYER_FOLLOWING, MemoryModuleState.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getMCABrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return this.checkExtraStartConditions(world, villager);
    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        PlayerEntity playerToFollow = villager.getMCABrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).get();
        villager.getNavigation().startMovingAlong(playerToFollow, villager.hasVehicle() ? 1.7D : 0.8D);
    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        PlayerEntity playerToFollow = villager.getMCABrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).get();

        double distance = villager.squaredDistanceTo(playerToFollow);
        if (distance >= 4.0D && distance <= 100.0D) {
            villager.getNavigation().startMovingAlong(playerToFollow, villager.hasVehicle() ? 1.7D : 0.8D);
        } else if (distance > 100.0D) {
            //teleportation when flying can kill the villager so we just let them walk on the surface. 
            villager.requestTeleport(playerToFollow.prevX, world.getTopY(Heightmap.Type.WORLD_SURFACE, (int) playerToFollow.prevX, (int) playerToFollow.prevZ), playerToFollow.prevZ);
        } else {
            villager.getNavigation().stop();
        }
    }
}

package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.minecraft.MemoryModuleTypeMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class FollowTask extends Task<VillagerEntityMCA> {

    public FollowTask() {
        super(ImmutableMap.of(MemoryModuleTypeMCA.PLAYER_FOLLOWING, MemoryModuleStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getMCABrain().getMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return this.checkExtraStartConditions(world, villager);
    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        PlayerEntity playerToFollow = villager.getMCABrain().getMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).get();
        villager.getNavigation().moveTo(playerToFollow, villager.isPassenger() ? 1.7D : 0.8D);
    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        PlayerEntity playerToFollow = villager.getMCABrain().getMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).get();

        double distance = villager.distanceToSqr(playerToFollow);
        if (distance >= 4.0D && distance <= 100.0D) {
            villager.getNavigation().moveTo(playerToFollow, villager.isPassenger() ? 1.7D : 0.8D);
        } else if (distance > 100.0D) {
            //teleportation when flying can kill the villager so we just let them walk on the surface. 
            villager.teleportTo(playerToFollow.xo, world.getHeight(Heightmap.Type.WORLD_SURFACE, (int) playerToFollow.xo, (int) playerToFollow.zo), playerToFollow.zo);
        } else {
            villager.getNavigation().stop();
        }
    }
}

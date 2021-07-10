package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class InteractTask extends Task<VillagerEntityMCA> {
    private final float speedModifier;

    public InteractTask(float speedModifie) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), Integer.MAX_VALUE);
        this.speedModifier = speedModifie;
    }

    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        PlayerEntity playerentity = villager.getInteractingPlayer();
        return villager.isAlive() && playerentity != null && !villager.isTouchingWater() && !villager.velocityModified && villager.squaredDistanceTo(playerentity) <= 16.0D && playerentity.currentScreenHandler != null && Chore.byId(villager.activeChore.get()) == Chore.NONE;
    }

    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return this.checkExtraStartConditions(world, villager);
    }

    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        this.followPlayer(villager);
    }

    protected void stop(ServerWorld world, VillagerEntityMCA villager, long p_212835_3_) {
        Brain<?> brain = villager.getBrain();
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.forget(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        this.followPlayer(villager);
    }

    protected boolean isTimeLimitExceeded(long p_220383_1_) {
        return false;
    }

    private void followPlayer(VillagerEntityMCA villager) {


        Brain<?> brain = villager.getBrain();
        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(villager.getInteractingPlayer(), false), this.speedModifier, 2));
        brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(villager.getInteractingPlayer(), true));
    }
}

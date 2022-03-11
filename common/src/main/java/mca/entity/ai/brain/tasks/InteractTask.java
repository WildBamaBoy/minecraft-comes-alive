package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Chore;
import mca.util.compat.OptionalCompat;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

public class InteractTask extends Task<VillagerEntityMCA> {
    private final float speedModifier;

    public InteractTask(float speedModifier) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED
        ), Integer.MAX_VALUE);
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return shouldRun(villager);
    }

    public static boolean shouldRun(VillagerEntityMCA villager) {
        return villager.isAlive()
                && villager.getInteractions().getInteractingPlayer().filter(player -> villager.squaredDistanceTo(player) <= 16).isPresent()
                && !villager.isTouchingWater()
                && !villager.velocityModified
                && villager.getVillagerBrain().getCurrentJob() == Chore.NONE;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return this.shouldRun(world, villager);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        this.followPlayer(villager);
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        Brain<?> brain = villager.getBrain();
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.forget(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        this.followPlayer(villager);
    }

    @Override
    protected boolean isTimeLimitExceeded(long p_220383_1_) {
        return false;
    }

    private void followPlayer(VillagerEntityMCA villager) {
        Brain<?> brain = villager.getBrain();

        OptionalCompat.ifPresentOrElse(villager.getInteractions().getInteractingPlayer(), player -> {
            brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(player, false), this.speedModifier, 2));
            brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(player, true));
        }, () -> {
            brain.forget(MemoryModuleType.WALK_TARGET);
            brain.forget(MemoryModuleType.LOOK_TARGET);
        });
    }
}

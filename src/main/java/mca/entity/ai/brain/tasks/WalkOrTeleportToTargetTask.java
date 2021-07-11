package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

//TODO make them teleport
public class WalkOrTeleportToTargetTask extends Task<VillagerEntityMCA> {
    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;

    public WalkOrTeleportToTargetTask() {
        this(150, 250);
    }

    public WalkOrTeleportToTargetTask(int minDuration, int maxDuration) {
        super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED, MemoryModuleType.PATH, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT), minDuration, maxDuration);
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        } else {
            Brain<?> brain = villager.getBrain();
            WalkTarget walktarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).get();
            boolean flag = this.reachedTarget(villager, walktarget);
            if (!flag && this.tryComputePath(villager, walktarget, world.getTime())) {
                this.lastTargetPos = walktarget.getLookTarget().getBlockPos();
                return true;
            } else {
                brain.forget(MemoryModuleType.WALK_TARGET);
                if (flag) {
                    brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                }

                return false;
            }
        }
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long gameTime) {
        if (this.path != null && this.lastTargetPos != null) {
            Optional<WalkTarget> optional = villager.getBrain().getOptionalMemory(MemoryModuleType.WALK_TARGET);
            EntityNavigation pathnavigator = villager.getNavigation();
            return !pathnavigator.isIdle() && optional.isPresent() && !this.reachedTarget(villager, optional.get());
        } else {
            return false;
        }
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long gameTime) {
        if (villager.getBrain().hasMemoryModule(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(villager, villager.getBrain().getOptionalMemory(MemoryModuleType.WALK_TARGET).get()) && villager.getNavigation().isNearPathStartPos()) {
            this.remainingCooldown = world.getRandom().nextInt(40);
        }

        villager.getNavigation().stop();
        villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
        villager.getBrain().forget(MemoryModuleType.PATH);
        this.path = null;
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        villager.getBrain().remember(MemoryModuleType.PATH, this.path);
        villager.getNavigation().startMovingAlong(this.path, this.speedModifier);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        Path path = villager.getNavigation().getCurrentPath();
        Brain<?> brain = villager.getBrain();
        if (this.path != path) {
            this.path = path;
            brain.remember(MemoryModuleType.PATH, path);
        }

        if (path != null && this.lastTargetPos != null) {
            WalkTarget walktarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).get();
            if (walktarget.getLookTarget().getBlockPos().getSquaredDistance(this.lastTargetPos) > 4.0D && this.tryComputePath(villager, walktarget, world.getTime())) {
                this.lastTargetPos = walktarget.getLookTarget().getBlockPos();
                this.run(world, villager, time);
            }

        }
    }

    private boolean tryComputePath(VillagerEntityMCA villager, WalkTarget walkTarget, long time) {
        BlockPos blockpos = walkTarget.getLookTarget().getBlockPos();
        this.path = villager.getNavigation().findPathTo(blockpos, 0);
        this.speedModifier = walkTarget.getSpeed();
        Brain<?> brain = villager.getBrain();
        if (this.reachedTarget(villager, walkTarget)) {
            brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean flag = this.path != null && this.path.reachesTarget();
            if (flag) {
                brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!brain.hasMemoryModule(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time);
            }

            if (this.path != null) {
                return true;
            }


            Vec3d vector3d = FuzzyTargeting.findTo(villager, 10, 7, Vec3d.ofBottomCenter(blockpos));
            if (vector3d != null) {
                this.path = villager.getNavigation().findPathTo(vector3d.x, vector3d.y, vector3d.z, 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(VillagerEntityMCA villager, WalkTarget walkTarget) {
        return walkTarget.getLookTarget().getBlockPos().getManhattanDistance(villager.getBlockPos()) <= walkTarget.getCompletionRange();
    }
}
package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

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
        super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.REGISTERED, MemoryModuleType.PATH, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_PRESENT), minDuration, maxDuration);
    }

    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        } else {
            Brain<?> brain = villager.getBrain();
            WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            boolean flag = this.reachedTarget(villager, walktarget);
            if (!flag && this.tryComputePath(villager, walktarget, world.getGameTime())) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
                return true;
            } else {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                if (flag) {
                    brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                }

                return false;
            }
        }
    }

    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long gameTime) {
        if (this.path != null && this.lastTargetPos != null) {
            Optional<WalkTarget> optional = villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            PathNavigator pathnavigator = villager.getNavigation();
            return !pathnavigator.isDone() && optional.isPresent() && !this.reachedTarget(villager, optional.get());
        } else {
            return false;
        }
    }

    protected void stop(ServerWorld world, VillagerEntityMCA villager, long gameTime) {
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(villager, villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get()) && villager.getNavigation().isStuck()) {
            this.remainingCooldown = world.getRandom().nextInt(40);
        }

        villager.getNavigation().stop();
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.path = null;
    }

    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        villager.getBrain().setMemory(MemoryModuleType.PATH, this.path);
        villager.getNavigation().moveTo(this.path, this.speedModifier);
    }

    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        Path path = villager.getNavigation().getPath();
        Brain<?> brain = villager.getBrain();
        if (this.path != path) {
            this.path = path;
            brain.setMemory(MemoryModuleType.PATH, path);
        }

        if (path != null && this.lastTargetPos != null) {
            WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            if (walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D && this.tryComputePath(villager, walktarget, world.getGameTime())) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
                this.start(world, villager, p_212833_3_);
            }

        }
    }

    private boolean tryComputePath(VillagerEntityMCA villager, WalkTarget walkTarget, long p_220487_3_) {
        BlockPos blockpos = walkTarget.getTarget().currentBlockPosition();
        this.path = villager.getNavigation().createPath(blockpos, 0);
        this.speedModifier = walkTarget.getSpeedModifier();
        Brain<?> brain = villager.getBrain();
        if (this.reachedTarget(villager, walkTarget)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean flag = this.path != null && this.path.canReach();
            if (flag) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, p_220487_3_);
            }

            if (this.path != null) {
                return true;
            }


            Vector3d vector3d = RandomPositionGenerator.getPosTowards(villager, 10, 7, Vector3d.atBottomCenterOf(blockpos));
            if (vector3d != null) {
                this.path = villager.getNavigation().createPath(vector3d.x, vector3d.y, vector3d.z, 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(VillagerEntityMCA villager, WalkTarget walkTarget) {
        return walkTarget.getTarget().currentBlockPosition().distManhattan(villager.blockPosition()) <= walkTarget.getCloseEnoughDist();
    }
}
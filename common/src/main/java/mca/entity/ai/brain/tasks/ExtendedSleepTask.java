package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import mca.entity.VillagerEntityMCA;
import mca.server.world.data.Building;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;

public class ExtendedSleepTask extends Task<VillagerEntityMCA> {
    private long startTime;
    private long cooldown;
    private float speed;
    private BlockPos bed;

    public ExtendedSleepTask(float speed) {
        super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryModuleState.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED));
        this.speed = speed;
    }

    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA entity) {
        if (entity.hasVehicle() && world.getTime() - cooldown > 40L) {
            return false;
        } else {
            cooldown = world.getTime();

            Brain<?> brain = entity.getBrain();
            GlobalPos globalPos = brain.getOptionalMemory(MemoryModuleType.HOME).get();
            if (world.getRegistryKey() != globalPos.getDimension()) {
                return false;
            } else {
                // wait a few seconds after wakeup
                Optional<Long> optional = brain.getOptionalMemory(MemoryModuleType.LAST_WOKEN);
                if (optional.isPresent()) {
                    long l = world.getTime() - optional.get();
                    if (l > 0L && l < 100L) {
                        return false;
                    }
                }

                // look for nearest bed
                Optional<Building> building = entity.getResidency().getHomeBuilding();
                if (building.isPresent()) {
                    Optional<BlockPos> bed = building.get().findClosestEmptyBed(world, globalPos.getPos());
                    if (bed.isPresent()) {
                        this.bed = bed.get();
                        if (globalPos.getPos().isWithinDistance(entity.getPos(), 2.0D)) {
                            return true;
                        } else {
                            brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(globalPos.getPos(), speed, 1));
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA entity, long time) {
        if (bed != null) {
            boolean distance = bed.isWithinDistance(entity.getPos(), 1.14D);
            return entity.getBrain().hasActivity(Activity.REST) && distance;
        } else {
            return false;
        }
    }

    protected void run(ServerWorld world, VillagerEntityMCA entity, long time) {
        if (time > this.startTime) {
            OpenDoorsTask.method_30760(world, entity, null, null);
            entity.sleep(bed);
        }
    }

    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    protected void finishRunning(ServerWorld world, VillagerEntityMCA entity, long time) {
        if (entity.isSleeping()) {
            entity.wakeUp();
            this.startTime = time + 40L;
        }
    }
}

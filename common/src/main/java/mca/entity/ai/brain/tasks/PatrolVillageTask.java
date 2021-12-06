package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import mca.server.world.data.Village;
import mca.util.BlockBoxExtended;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PatrolVillageTask extends Task<VillagerEntityMCA> {
    private final int completionRange;
    private final float speed;

    public PatrolVillageTask(int completionRange, float speed) {
        super(ImmutableMap.of(
                MemoryModuleTypeMCA.PLAYER_FOLLOWING, MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED));
        this.completionRange = completionRange;
        this.speed = speed;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA entity) {
        return !InteractTask.shouldRun(entity);
    }

    protected void run(ServerWorld serverWorld, VillagerEntityMCA villager, long l) {
        Optional<BlockPos> blockPos = getNextPosition(villager);
        blockPos.ifPresent(pos -> LookTargetUtil.walkTowards(villager, pos, this.speed, this.completionRange));
    }

    private Optional<BlockPos> getNextPosition(VillagerEntityMCA villager) {
        Optional<Village> village = villager.getResidency().getHomeVillage();
        if (village.isPresent()) {
            BlockBoxExtended box = village.get().getBox();
            int x = box.minX + villager.getRandom().nextInt(box.getBlockCountX());
            int z = box.minZ + villager.getRandom().nextInt(box.getBlockCountZ());
            Vec3d targetPos = new Vec3d(x, box.getCenter().getY(), z);
            Vec3d towards = TargetFinder.findGroundTargetTowards(villager, 32, 16, 0, targetPos, Math.PI * 0.5);
            return towards == null ? Optional.empty() : Optional.of(new BlockPos(towards));
        } else {
            return Optional.empty();
        }
    }
}

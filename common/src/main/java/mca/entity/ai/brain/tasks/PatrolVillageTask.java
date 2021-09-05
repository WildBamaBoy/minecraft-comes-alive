package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import mca.entity.VillagerEntityMCA;
import mca.server.world.data.Village;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PatrolVillageTask extends Task<VillagerEntityMCA> {
    private final int completionRange;
    private final float speed;

    public PatrolVillageTask(int completionRange, float speed) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED));
        this.completionRange = completionRange;
        this.speed = speed;
    }

    protected void run(ServerWorld serverWorld, VillagerEntityMCA villager, long l) {
        Optional<BlockPos> blockPos = getNextPosition(villager);
        blockPos.ifPresent(pos -> LookTargetUtil.walkTowards(villager, pos, this.speed, this.completionRange));
    }

    private Optional<BlockPos> getNextPosition(VillagerEntityMCA villager) {
        Optional<Village> village = villager.getResidency().getHomeVillage();
        if (village.isPresent()) {
            BlockPos center = village.get().getCenter();
            int size = village.get().getSize();
            int x = center.getX() + villager.getRandom().nextInt(size * 2) - size;
            int z = center.getZ() + villager.getRandom().nextInt(size * 2) - size;
            Vec3d targetPos = new Vec3d(x, center.getY(), z);
            Vec3d towards = TargetFinder.findGroundTargetTowards(villager, 32, 16, 0, targetPos, Math.PI * 0.5);
            return towards == null ? Optional.empty() : Optional.of(new BlockPos(towards));
        } else {
            return Optional.empty();
        }
    }
}

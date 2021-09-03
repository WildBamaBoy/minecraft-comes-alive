package mca.entity.ai.brain.tasks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public class ExtendedMeleeAttackTask extends MeleeAttackTask {
    private final float range;

    public ExtendedMeleeAttackTask(int interval, float range) {
        super(interval);
        this.range = range;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, MobEntity attacker) {
        LivingEntity target = getTarget(attacker);
        return LookTargetUtil.isVisibleInMemory(attacker, target) && withinRange(attacker, target);
    }

    private boolean withinRange(LivingEntity attacker, LivingEntity target) {
        double d = attacker.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
        double r = attacker.getWidth() + target.getWidth() + range;
        return d <= r;
    }

    private LivingEntity getTarget(MobEntity mobEntity) {
        return mobEntity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

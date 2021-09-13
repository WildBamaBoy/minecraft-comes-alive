package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class ExtendedMeleeAttackTask extends Task<MobEntity> {
    private final float range;
    private final int interval;
    private final MemoryModuleType<? extends LivingEntity> target;

    public ExtendedMeleeAttackTask(int interval, float range) {
        this(interval, range, MemoryModuleType.ATTACK_TARGET);
    }

    public ExtendedMeleeAttackTask(int interval, float range, MemoryModuleType<? extends LivingEntity> target) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, target, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT));
        this.range = range;
        this.interval = interval;
        this.target = target;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, MobEntity attacker) {
        LivingEntity target = getTarget(attacker);
        return LookTargetUtil.isVisibleInMemory(attacker, target) && withinRange(attacker, target);
    }

    @Override
    protected void run(ServerWorld serverWorld, MobEntity mobEntity, long l) {
        LivingEntity livingEntity = getTarget(mobEntity);
        LookTargetUtil.lookAt(mobEntity, livingEntity);
        mobEntity.swingHand(Hand.MAIN_HAND);
        mobEntity.tryAttack(livingEntity);
        mobEntity.getBrain().remember(MemoryModuleType.ATTACK_COOLING_DOWN, true, interval);
    }

    private boolean withinRange(LivingEntity attacker, LivingEntity target) {
        double d = attacker.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
        double r = attacker.getWidth() + target.getWidth() + range;
        return d <= r;
    }

    private LivingEntity getTarget(MobEntity mobEntity) {
        return mobEntity.getBrain().getOptionalMemory(target).get();
    }
}

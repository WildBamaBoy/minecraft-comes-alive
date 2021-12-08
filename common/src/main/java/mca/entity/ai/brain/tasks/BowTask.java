package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

public class BowTask<E extends MobEntity & CrossbowUser> extends Task<E> {
    private int lastShot;
    private final int fireInterval;
    private final int squaredRange;

    public BowTask(int fireInterval, int range) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT));
        this.fireInterval = fireInterval;
        this.squaredRange = range * range;
    }

    protected boolean shouldRun(ServerWorld serverWorld, E entity) {
        LivingEntity livingEntity = getAttackTarget(entity);
        return livingEntity != null && entity.isHolding(Items.BOW)
                && LookTargetUtil.isVisibleInMemory(entity, livingEntity)
                && LookTargetUtil.method_25940(entity, livingEntity, 0);
    }

    @Override
    protected void keepRunning(ServerWorld world, E entity, long time) {
        super.keepRunning(world, entity, time);

        LivingEntity target = getAttackTarget(entity);
        double d = entity.squaredDistanceTo(target);

        //keep distance
        float backward = 0.0f;
        if (d > this.squaredRange * 1.25F) {
            backward = 0.5f;
        } else if (d < this.squaredRange * 0.75F) {
            backward = -0.5f;
        }

        //strafe
        float strafe = (float)(Math.cos(time / 20.0f) * 0.5);
        entity.getMoveControl().strafeTo(backward, strafe);
        entity.lookAtEntity(target, 30.0F, 30.0F);

        //shoot
        if (entity.age - lastShot > fireInterval) {
            entity.attack(target, 1.0F);
            lastShot = entity.age;
        }
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, E entity, long time) {
        return shouldRun(world, entity);
    }

    @Override
    protected void run(ServerWorld world, E entity, long time) {
        entity.setAttacking(true);
    }

    private static LivingEntity getAttackTarget(LivingEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Override
    protected void finishRunning(ServerWorld world, E entity, long time) {
        super.finishRunning(world, entity, time);
        entity.setAttacking(false);
    }
}

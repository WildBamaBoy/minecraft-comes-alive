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

	public BowTask(int fireInterval) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
				MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT));
		this.fireInterval = fireInterval;
	}

	protected boolean shouldRun(ServerWorld serverWorld, E mobEntity) {
		LivingEntity livingEntity = getAttackTarget(mobEntity);
		return mobEntity.age - lastShot > fireInterval && mobEntity.isHolding(Items.BOW)
				&& LookTargetUtil.isVisibleInMemory(mobEntity, livingEntity)
				&& LookTargetUtil.method_25940(mobEntity, livingEntity, 0);
	}

	@Override
	protected void run(ServerWorld world, E entity, long time) {
		entity.attack(getAttackTarget(entity), 1.0F);
		lastShot = entity.age;
	}

	private static LivingEntity getAttackTarget(LivingEntity entity) {
		return entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
}

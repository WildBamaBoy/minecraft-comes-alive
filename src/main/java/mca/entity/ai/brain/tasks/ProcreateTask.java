package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class ProcreateTask extends Task<EntityVillagerMCA> {
    public ProcreateTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, EntityVillagerMCA villager) {
        return villager.isProcreating.get();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, EntityVillagerMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager);
    }

    @Override
    protected void tick(ServerWorld world, EntityVillagerMCA villager, long p_212833_3_) {
        Random random = villager.getRandom();
        if (villager.procreateTick > 0) {
            villager.procreateTick--;

            world.broadcastEntityEvent(villager, (byte) 12);
        } else {
            ItemStack stack = new ItemStack(random.nextBoolean() ? MCA.ITEM_BABY_BOY.get() : MCA.ITEM_BABY_GIRL.get());
            villager.getInventory().addItem(stack);
            villager.isProcreating.set(false);
        }
    }
}

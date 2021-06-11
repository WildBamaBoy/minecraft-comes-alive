package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.minecraft.ItemsMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class ProcreateTask extends Task<VillagerEntityMCA> {
    public ProcreateTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.isProcreating.get();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager);
    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        Random random = villager.getRandom();
        if (villager.procreateTick > 0) {
            villager.procreateTick--;
            villager.getNavigation().stop();
            world.broadcastEntityEvent(villager, (byte) 12);
        } else {
            ItemStack stack = new ItemStack(random.nextBoolean() ? ItemsMCA.BABY_BOY.get() : ItemsMCA.BABY_GIRL.get());
            PlayerEntity player = villager.level.getPlayerByUUID(villager.spouseUUID.get().get());
            if (player != null) {
                if (!player.addItem(stack)) {
                    villager.getInventory().addItem(stack);
                }
            } else {
                villager.getInventory().addItem(stack);
            }
            villager.isProcreating.set(false);
        }
    }
}

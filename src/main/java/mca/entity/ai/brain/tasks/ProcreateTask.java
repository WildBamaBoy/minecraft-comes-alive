package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.minecraft.ItemsMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import java.util.Random;

public class ProcreateTask extends Task<VillagerEntityMCA> {
    public ProcreateTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.isProcreating.get();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return shouldRun(world, villager);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        Random random = villager.getRandom();
        if (villager.procreateTick > 0) {
            villager.procreateTick--;
            villager.getNavigation().stop();
            world.sendEntityStatus(villager, (byte) 12);
        } else {
            //make sure this village is registered in the family tree
            villager.getFamilyTree().addEntry(villager);

            ItemStack stack = new ItemStack(random.nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
            PlayerEntity player = villager.world.getPlayerByUuid(villager.spouseUUID.get().get());
            if (player != null) {
                if (!player.giveItemStack(stack)) {
                    villager.getInventory().addStack(stack);
                }
            } else {
                villager.getInventory().addStack(stack);
            }
            villager.isProcreating.set(false);
        }
    }
}

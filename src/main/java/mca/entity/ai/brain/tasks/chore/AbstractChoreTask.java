package mca.entity.ai.brain.tasks.chore;

import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractChoreTask extends Task<VillagerEntityMCA> {
    protected VillagerEntityMCA villager;

    public AbstractChoreTask(Map<MemoryModuleType<?>, MemoryModuleStatus> p_i51504_1_) {
        super(p_i51504_1_);
    }


    @Override
    protected void tick(ServerWorld p_212833_1_, VillagerEntityMCA p_212833_2_, long p_212833_3_) {
        if (!getAssigningPlayer().isPresent()) {
            MCA.log("Force-stopped chore because assigning player was not present.");
            villager.stopChore();
        }
    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        this.villager = villager;
    }

    Optional<PlayerEntity> getAssigningPlayer() {
        PlayerEntity player = villager.level.getPlayerByUUID(villager.choreAssigningPlayer.get().get());
        return Optional.ofNullable(player);
    }
}

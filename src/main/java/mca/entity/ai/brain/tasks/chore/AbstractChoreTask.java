package mca.entity.ai.brain.tasks.chore;

import mca.MCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractChoreTask extends Task<VillagerEntityMCA> {
    protected VillagerEntityMCA villager;

    public AbstractChoreTask(Map<MemoryModuleType<?>, MemoryModuleState> p_i51504_1_) {
        super(p_i51504_1_);
    }


    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA entity, long time) {
        if (!getAssigningPlayer().isPresent()) {
            MCA.logger.info("Force-stopped chore because assigning player was not present.");
            villager.getVillagerBrain().abandonJob();
        }
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA entity, long time) {
        this.villager = entity;
    }

    Optional<PlayerEntity> getAssigningPlayer() {
        return villager.getVillagerBrain().getJobAssigner();
    }

    void abandonJobWithMessage(String message) {
        getAssigningPlayer().ifPresent(player -> {
            villager.sendChatMessage(player, message);
        });
        villager.getVillagerBrain().abandonJob();
    }
}

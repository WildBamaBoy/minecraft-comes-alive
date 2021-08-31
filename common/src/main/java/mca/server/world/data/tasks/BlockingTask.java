package mca.server.world.data.tasks;

import mca.server.world.data.Rank;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;

public class BlockingTask extends Task {
    public BlockingTask(Rank rank, String id) {
        super(rank, id);
    }

    @Override
    public boolean isCompleted(Village village, ServerPlayerEntity player) {
        return false;
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}

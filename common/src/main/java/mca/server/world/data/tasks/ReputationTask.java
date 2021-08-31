package mca.server.world.data.tasks;

import mca.server.world.data.Rank;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class ReputationTask extends Task {
    private final int reputation;

    public ReputationTask(Rank rank, int reputation) {
        super(rank, rank.toString() + reputation);
        this.reputation = reputation;
    }

    @Override
    public boolean isCompleted(Village village, ServerPlayerEntity player) {
        return village.getReputation(player) >= reputation;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public TranslatableText getTranslatable() {
        return new TranslatableText("task.reputation", reputation);
    }
}

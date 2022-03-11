package mca.resources.data.tasks;

import com.google.gson.JsonObject;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.JsonHelper;

public class ReputationTask extends Task {
    private static final long serialVersionUID = -7232675787774372089L;

    private final int reputation;

    public ReputationTask(int reputation) {
        super("reputation_" + reputation);
        this.reputation = reputation;
    }

    public ReputationTask(JsonObject json) {
        this(JsonHelper.getInt(json, "reputation"));
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

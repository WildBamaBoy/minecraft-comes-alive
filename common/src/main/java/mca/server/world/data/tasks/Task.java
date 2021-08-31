package mca.server.world.data.tasks;

import mca.server.world.data.Rank;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public abstract class Task {
    private final Rank rank;
    private final String id;

    public Task(Rank rank, String id) {
        this.rank = rank;
        this.id = id;
    }

    abstract public boolean isCompleted(Village village, ServerPlayerEntity player);

    public boolean isRequired() {
        return false;
    }

    public Rank getRank() {
        return rank;
    }

    public TranslatableText getTranslatable() {
        return new TranslatableText("task." + id);
    }

    public String getId() {
        return id;
    }
}

package mca.resources.data.tasks;

import com.google.gson.JsonObject;
import java.util.Objects;
import mca.server.world.data.Village;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class AdvancementTask extends Task {
    private final String identifier;

    public AdvancementTask(String identifier) {
        super("advancement_" + identifier);
        this.identifier = identifier;
    }

    public AdvancementTask(JsonObject json) {
        this(JsonHelper.getString(json, "id"));
    }

    @Override
    public boolean isCompleted(Village village, ServerPlayerEntity player) {
        Advancement advancement = Objects.requireNonNull(player.getServer()).getAdvancementLoader().get(new Identifier(identifier));
        return player.getAdvancementTracker().getProgress(advancement).isDone();
    }
}

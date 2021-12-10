package mca.resources.data.tasks;

import com.google.gson.JsonObject;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.JsonHelper;

public class BuildingTask extends Task {
    private static final long serialVersionUID = -6660910729161211245L;

    private final String type;

    public BuildingTask(String type) {
        super(type);
        this.type = type;
    }

    public BuildingTask(JsonObject json) {
        this(JsonHelper.getString(json, "building"));
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isCompleted(Village village, ServerPlayerEntity player) {
        return village.getBuildings().values().stream()
                .anyMatch(b -> b.getType().equals(type));
    }

    @Override
    public TranslatableText getTranslatable() {
        return new TranslatableText("task.build", new TranslatableText("buildingType." + type));
    }
}

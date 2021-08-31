package mca.server.world.data.tasks;

import mca.server.world.data.Rank;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class BuildingTask extends Task {
    private final String type;

    public BuildingTask(Rank rank, String type) {
        super(rank, type);
        this.type = type;
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

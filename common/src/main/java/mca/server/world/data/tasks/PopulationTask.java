package mca.server.world.data.tasks;

import mca.server.world.data.Rank;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class PopulationTask extends Task {
    private final int population;

    public PopulationTask(Rank rank, int population) {
        super(rank, rank.toString() + population);
        this.population = population;
    }

    @Override
    public boolean isCompleted(Village village, ServerPlayerEntity player) {
        return village.getPopulation() >= population;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public TranslatableText getTranslatable() {
        return new TranslatableText("task.population", population);
    }
}

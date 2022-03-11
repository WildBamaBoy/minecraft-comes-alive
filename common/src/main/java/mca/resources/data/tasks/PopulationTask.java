package mca.resources.data.tasks;

import com.google.gson.JsonObject;
import mca.server.world.data.Village;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.JsonHelper;

public class PopulationTask extends Task {
    private static final long serialVersionUID = 5252203744206810361L;

    private final int population;

    public PopulationTask(int population) {
        super("population_" + population);
        this.population = population;
    }

    public PopulationTask(JsonObject json) {
        this(JsonHelper.getInt(json, "population"));
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

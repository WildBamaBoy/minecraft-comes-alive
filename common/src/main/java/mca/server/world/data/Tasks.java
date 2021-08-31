package mca.server.world.data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;
import mca.server.world.data.tasks.BlockingTask;
import mca.server.world.data.tasks.BuildingTask;
import mca.server.world.data.tasks.PopulationTask;
import mca.server.world.data.tasks.ReputationTask;
import mca.server.world.data.tasks.Task;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;

public class Tasks {
    //TODO convert to Json
    public static final List<Task> TASKS = Arrays.asList(
            new ReputationTask(Rank.PEASANT, 0),

            new ReputationTask(Rank.MERCHANT, 20),
            new BuildingTask(Rank.MERCHANT, "bigHouse"),
            new BuildingTask(Rank.MERCHANT, "storage"),

            new ReputationTask(Rank.NOBLE, 40),
            new BuildingTask(Rank.NOBLE, "inn"),
            new BuildingTask(Rank.NOBLE, "graveyard"),

            new ReputationTask(Rank.MAYOR, 60),
            new PopulationTask(Rank.MAYOR, 20),
            new BuildingTask(Rank.MAYOR, "library"),
            new BuildingTask(Rank.MAYOR, "armory"),

            new ReputationTask(Rank.KING, 80),
            new PopulationTask(Rank.KING, 30),
            new BuildingTask(Rank.KING, "prison"),
            new BuildingTask(Rank.KING, "blacksmith"),
            new BlockingTask(Rank.KING, "grimReaper"),
            new BlockingTask(Rank.KING, "bePatient")
    );

    public static Set<String> getCompletedIds(Village village, ServerPlayerEntity player) {
        return TASKS.stream().filter(t -> t.isCompleted(village, player)).map(Task::getId).collect(Collectors.toSet());
    }

    public static Rank getRank(Village village, ServerPlayerEntity player) {
        return TASKS.stream().filter(t -> t.isRequired() && !t.isCompleted(village, player))
                .map(Task::getRank).min(Comparator.comparingInt(Enum::ordinal))
                .map(Rank::degrade).orElse(Rank.OUTLAW);
    }
}

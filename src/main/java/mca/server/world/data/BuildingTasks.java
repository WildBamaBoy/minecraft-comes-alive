package mca.server.world.data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import mca.entity.ai.Rank;
import mca.util.NbtHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class BuildingTasks {
    public static final List<String> NAMES = List.of("buildBigHouse", "buildStorage", "buildInn", "bePatient");
    private static final Map<String, String> TASK_BUILDINGS = Map.of(
            "bigHouse", "buildBigHouse",
            "storage", "buildStorage",
            "inn", "buildInn"
    );

    public static class Tasks {
        private Set<String> completedTasks = new HashSet<>();

        private final Village village;

        public Tasks(Village village) {
            this.village = village;
        }

        public void init() {
            completedTasks = village.getBuildings().values().stream()
                    .map(Building::getType)
                    .distinct()
                    .map(TASK_BUILDINGS::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        public void setCompleted(String taskName, boolean completed) {
            if (completed) {
                completedTasks.add(taskName);
            } else {
                completedTasks.remove(taskName);
            }
        }

        public boolean isCompleted(String taskName) {
            return completedTasks.contains(taskName);
        }

        /**
         * Returns the index of the first incomplete task.
         */
        public int getTotalCompleted() {
            return completedTasks.size();
        }

        public Rank getRank(int reputation) {
            return Rank.getClamped(getTotalCompleted(), reputation);
        }

        public void load(NbtCompound nbt) {
            if (nbt.contains("tasks", NbtElement.LIST_TYPE)) {
                completedTasks = NbtHelper.toStream(nbt.getList("tasks", NbtElement.STRING_TYPE), i -> ((NbtString)i).asString()).collect(Collectors.toSet());
            }
        }

        public void save(NbtCompound nbt) {
            nbt.put("tasks", NbtHelper.fromList(completedTasks, NbtString::of));
        }
    }
}

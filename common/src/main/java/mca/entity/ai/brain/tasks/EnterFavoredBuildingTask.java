package mca.entity.ai.brain.tasks;

import java.util.Optional;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.server.world.data.Building;
import net.minecraft.util.math.BlockPos;

public class EnterFavoredBuildingTask extends EnterBuildingTask {
    private int lastMoodIncrease = 0;
    private static final int TICKS_PER_MOOD = 1200;

    public EnterFavoredBuildingTask(float speed) {
        super("", speed);
    }

    @Override
    public String getBuilding(VillagerEntityMCA villager) {
        String building = villager.getVillagerBrain().getMood().getBuilding();
        if (building != null) {
            return building;
        } else {
            return ProfessionsMCA.getFavoredBuilding(villager.getProfession());
        }
    }

    @Override
    protected Optional<BlockPos> getNextPosition(VillagerEntityMCA villager) {
        Optional<Building> b = getNearestBuilding(villager);
        if (b.isPresent()) {
            if (b.get().containsPos(villager.getBlockPos())) {
                if (villager.age > lastMoodIncrease + TICKS_PER_MOOD && villager.getVillagerBrain().getMoodValue() < 0) {
                    lastMoodIncrease = villager.age;
                    villager.getVillagerBrain().modifyMoodValue(1);
                }
            } else {
                return getRandomPositionIn(b.get(), villager.world);
            }
        }
        return Optional.empty();
    }
}

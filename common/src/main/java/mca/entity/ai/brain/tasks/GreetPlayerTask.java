package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import mca.Config;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Memories;
import mca.entity.ai.Relationship;
import mca.server.world.data.PlayerSaveData;
import mca.server.world.data.Village;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class GreetPlayerTask extends Task<VillagerEntityMCA> {
    private static final int MAX_COOLDOWN = 2000;

    private Optional<? extends PlayerEntity> target = Optional.empty();

    private int cooldown;

    private boolean talked;

    public GreetPlayerTask() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.REGISTERED
        ), 600);
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        cooldown = MAX_COOLDOWN;
        return getPlayer(villager).isPresent();
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        target = getPlayer(villager);
        target.ifPresent(player -> {
            villager.getBrain().remember(MemoryModuleType.INTERACTION_TARGET, player);
            LookTargetUtil.lookAt(villager, player);
            talked = false;
        });
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return !talked && !villager.getVillagerBrain().isPanicking();
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        target.ifPresent(player -> {
            LookTargetUtil.lookAt(villager, player);

            if (isWithinGreetingDistance(villager, player)) {
                Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
                int day = (int)(villager.world.getTimeOfDay() / 24000L);
                memories.setLastSeen(day);

                String phrase = memories.getHearts() < 0 ? "welcomeFoe" : "welcome";
                villager.sendChatMessage(player, phrase, player.getName());
                talked = true;

                villager.playWelcomeSound();
            } else {
                LookTargetUtil.walkTowards(villager, player, 0.55F, 2);
            }
        });
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        // missed the player this time
        if (!talked && target.isPresent()) {
            Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(target.get());
            memories.setLastSeen(-1);
        }

        target = Optional.empty();
        villager.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
        villager.getBrain().forget(MemoryModuleType.LOOK_TARGET);
    }

    private static Optional<? extends PlayerEntity> getPlayer(VillagerEntityMCA villager) {
        return villager.world.getPlayers().stream()
                .filter(p -> shouldGreet(villager, p))
                .findFirst();
    }

    private static boolean shouldGreet(VillagerEntityMCA villager, PlayerEntity player) {
        Optional<Integer> id = PlayerSaveData.get((ServerWorld)player.world, player.getUuid()).getLastSeenVillageId();
        Optional<Village> village = villager.getResidency().getHomeVillage();
        if (id.isPresent() && village.isPresent() && id.get() == village.get().getId()) {
            Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);

            int day = (int)(villager.world.getTimeOfDay() / 24000L);

            // first check relationships, only family, friends and foes will greet you
            if (Relationship.IS_MARRIED.test(villager, player)
                    || Relationship.IS_RELATIVE.test(villager, player)
                    || Math.abs(memories.getHearts()) >= Config.getInstance().greetHeartsThreshold) {
                long diff = day - memories.getLastSeen();

                if (diff > Config.getInstance().greetAfterDays && memories.getLastSeen() > 0) {
                    return true;
                }

                if (diff > 0) {
                    //there is a diff, but not long enough
                    memories.setLastSeen(day);
                }
            } else {
                //no interest
                memories.setLastSeen(day);
            }
        }

        return false;
    }

    private static boolean isWithinGreetingDistance(VillagerEntityMCA villager, PlayerEntity player) {
        return villager.getBlockPos().isWithinDistance(player.getBlockPos(), 3);
    }
}

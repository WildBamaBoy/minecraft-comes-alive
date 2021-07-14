package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.MCA;
import mca.entity.Relationship;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Memories;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;

public class GreetPlayerTask extends Task<VillagerEntityMCA> {
    private static final int MAX_COOLDOWN = 200;

    private Optional<? extends PlayerEntity> target = Optional.empty();

    private int cooldown;

    private boolean talked;
    private int talking;

    public GreetPlayerTask() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleState.VALUE_PRESENT
        ));
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
        return !talked || talking > 0;
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        target.ifPresent(player -> {
            LookTargetUtil.lookAt(villager, player);

            if (isWithinGreetingDistance(villager, player)) {
                if (!talked) {
                    Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
                    int day = (int) (villager.world.getTimeOfDay() / 24000L);
                    memories.setLastSeen(day);

                    String phrase = memories.getHearts() <= MCA.getConfig().greetHeartsThreshold ? "welcomeFoe" : "welcome";
                    villager.sendChatMessage(player, phrase, player.getName());
                    talked = true;
                    talking = MAX_COOLDOWN;

                    villager.playWelcomeSound();
                }
                talking--;
            } else {
                LookTargetUtil.walkTowards(villager, player, 0.5F, 3);
            }
        });
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
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

        Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);

        int day = (int) (villager.world.getTimeOfDay() / 24000L);

        // first check relationships, only family, friends and foes will greet you
        if (!Relationship.IS_MARRIED.test(villager, player)
                || Relationship.IS_RELATIVE.test(villager, player)
                || Math.abs(memories.getHearts()) >= MCA.getConfig().greetHeartsThreshold) {
            long diff = day - memories.getLastSeen();

            if (diff > MCA.getConfig().greetAfterDays && memories.getLastSeen() > 0) {
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

        return false;
    }

    private static boolean isWithinGreetingDistance(VillagerEntityMCA villager, PlayerEntity player) {
        return villager.getBlockPos().isWithinDistance(player.getBlockPos(), 3);
    }
}

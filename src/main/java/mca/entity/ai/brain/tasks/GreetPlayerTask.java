package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.FamilyTree;
import mca.entity.data.Memories;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class GreetPlayerTask extends Task<VillagerEntityMCA> {
    private int cooldown;
    private PlayerEntity target;

    private boolean talked;
    private int talking;
    private static final int talkTime = 10 * 20;

    public GreetPlayerTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleStatus.VALUE_PRESENT));
    }

    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        if (cooldown > 0) {
            cooldown--;
            return false;
        } else {
            cooldown = 200;
            return getPlayer(villager).isPresent();
        }
    }

    protected void start(ServerWorld world, VillagerEntityMCA villager, long time) {
        Optional<PlayerEntity> player = getPlayer(villager);
        if (player.isPresent()) {
            target = player.get();
            villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, target);
            BrainUtil.lookAtEntity(villager, target);
            talked = false;
        }
    }

    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long time) {
        return !talked || talking > 0;
    }

    protected void tick(ServerWorld world, VillagerEntityMCA villager, long time) {
        BrainUtil.lookAtEntity(villager, target);
        if (isWithinGreetingDistance(villager, target)) {
            if (!talked) {
                Memories memories = villager.getMemoriesForPlayer(target);
                int day = (int) (villager.level.getGameTime() % 24000);
                memories.setLastSeen(day);

                String phrase = memories.getHearts() <= MCA.getConfig().greetHeartsThreshold ? "welcomeFoe" : "welcome";
                villager.say(target, phrase, target.getName().getContents());
                talked = true;
                talking = talkTime;

                villager.playWelcomeSound();
            }
            talking--;
        } else {
            BrainUtil.setWalkAndLookTargetMemories(villager, target, 0.5F, 3);
        }
     }

    protected void stop(ServerWorld world, VillagerEntityMCA villager, long time) {
        target = null;
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    private Optional<PlayerEntity> getPlayer(VillagerEntityMCA villager) {
        return (Optional<PlayerEntity>) villager.level.players().stream().filter(p -> shouldGreet(villager, p)).findFirst();
    }

    private boolean shouldGreet(VillagerEntityMCA villager, PlayerEntity player) {
        //first check relationships, only family, friends and foes will greet you
        boolean isRelative = FamilyTree.get(villager.level).isRelative(villager.getUUID(), player.getUUID());
        boolean isSpouse = villager.spouseUUID.get().isPresent() && villager.spouseUUID.get().get().equals(player.getUUID());
        Memories memories = villager.getMemoriesForPlayer(player);
        int day = (int) (villager.level.getGameTime() % 24000);

        if (isSpouse || isRelative || Math.abs(memories.getHearts()) >= MCA.getConfig().greetHeartsThreshold) {
            int diff = day - memories.getLastSeen();

            if (diff > MCA.getConfig().greetAfterDays && memories.getLastSeen() > 0) {
                return true;
            } else if (diff > 0) {
                //there is a diff, but not long enough
                memories.setLastSeen(day);
            }
        } else {
            //no interest
            memories.setLastSeen(day);
        }
        return false;
    }

    private boolean isWithinGreetingDistance(VillagerEntityMCA villager, PlayerEntity player) {
        BlockPos p = player.blockPosition();
        BlockPos v = villager.blockPosition();
        return v.closerThan(p, 3.0D);
    }
}

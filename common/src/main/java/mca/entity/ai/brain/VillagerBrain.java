package mca.entity.ai.brain;

import java.util.HashMap;
import java.util.Map;
import mca.Config;
import mca.advancement.criterion.CriterionMCA;
import mca.entity.Status;
import mca.entity.VillagerLike;
import mca.entity.ai.*;
import mca.entity.ai.relationship.Personality;
import mca.util.network.datasync.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles memory and complex bodily functions. Such as walking, and not being a nitwit.
 */
public class VillagerBrain<E extends MobEntity & VillagerLike<E>> {
    private static final CDataParameter<NbtCompound> MEMORIES = CParameter.create("memories", new NbtCompound());
    private static final CEnumParameter<Personality> PERSONALITY = CParameter.create("personality", Personality.UNASSIGNED);
    private static final CDataParameter<Integer> MOOD = CParameter.create("mood", 0);
    private static final CEnumParameter<MoveState> MOVE_STATE = CParameter.create("moveState", MoveState.MOVE);
    private static final CEnumParameter<Chore> ACTIVE_CHORE = CParameter.create("activeChore", Chore.NONE);
    private static final CDataParameter<Optional<UUID>> CHORE_ASSIGNING_PLAYER = CParameter.create("choreAssigningPlayer", Optional.empty());
    private static final CDataParameter<Boolean> PANICKING = CParameter.create("isPanicking", false);
    private static final CDataParameter<Boolean> WEAR_ARMOR = CParameter.create("wearArmor", false);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder.addAll(MEMORIES, PERSONALITY, MOOD, MOVE_STATE, ACTIVE_CHORE, CHORE_ASSIGNING_PLAYER, PANICKING, WEAR_ARMOR);
    }

    private final E entity;

    public VillagerBrain(E entity) {
        this.entity = entity;
    }

    public void think() {
        // When you relog, it should continue doing the chores.
        // Chore saves but Activity doesn't, so this checks if the activity is not on there and puts it on there.

        if (entity.getTrackedValue(ACTIVE_CHORE) != Chore.NONE) {
            // find something to do
            entity.getBrain().getFirstPossibleNonCoreActivity().ifPresent(activity -> {
                if (!activity.equals(ActivityMCA.CHORE)) {
                    entity.getBrain().doExclusively(ActivityMCA.CHORE);
                }
            });
        }

        boolean panicking = entity.getBrain().hasActivity(Activity.PANIC);
        if (panicking != entity.getTrackedValue(PANICKING)) {
            entity.setTrackedValue(PANICKING, panicking);
        }

        if (entity.age % 20 != 0) {
            updateMoveState();
        }

        // decrease interaction fatigue
        if (entity.age % Config.getInstance().interactionFatigueCooldown == 0) {
            NbtCompound nbt = entity.getTrackedValue(MEMORIES);
            if (nbt != null) {
                for (String uuid : nbt.getKeys()) {
                    Memories memories = Memories.fromCNBT(entity, nbt.getCompound(uuid));
                    int fatigue = memories.getInteractionFatigue();
                    if (fatigue > 0) {
                        memories.setInteractionFatigue(fatigue - 1);
                    }
                }
            }
        }
    }

    public Chore getCurrentJob() {
        return entity.getTrackedValue(ACTIVE_CHORE);
    }

    public Optional<PlayerEntity> getJobAssigner() {
        return entity.getTrackedValue(CHORE_ASSIGNING_PLAYER).map(id -> entity.world.getPlayerByUuid(id));
    }

    /**
     * Tells the villager to stop doing whatever it's doing.
     */
    public void abandonJob() {
        entity.getBrain().doExclusively(Activity.IDLE);
        entity.setTrackedValue(ACTIVE_CHORE, Chore.NONE);
        entity.setTrackedValue(CHORE_ASSIGNING_PLAYER, Optional.empty());
    }

    /**
     * Assigns a job for the villager to do.
     */
    public void assignJob(Chore chore, PlayerEntity player) {
        entity.getBrain().doExclusively(ActivityMCA.CHORE);
        entity.setTrackedValue(ACTIVE_CHORE, chore);
        entity.setTrackedValue(CHORE_ASSIGNING_PLAYER, Optional.of(player.getUuid()));
        entity.getBrain().forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
        entity.getBrain().forget(MemoryModuleTypeMCA.STAYING);
    }

    public void randomize() {
        entity.setTrackedValue(PERSONALITY, Personality.getRandom());
        entity.setTrackedValue(MOOD, entity.world.random.nextInt(MoodGroup.maxLevel - MoodGroup.normalMinLevel + 1) + MoodGroup.normalMinLevel);
    }

    public void setPersonality(Personality p) {
        entity.setTrackedValue(PERSONALITY, p);
    }

    public void updateMemories(Memories memories) {
        NbtCompound nbt = entity.getTrackedValue(MEMORIES);

        nbt = nbt == null ? new NbtCompound() : nbt.copy();
        nbt.put(memories.getPlayerUUID().toString(), memories.toCNBT());
        entity.setTrackedValue(MEMORIES, nbt);
    }

    public Map<UUID, Memories> getMemories() {
        NbtCompound nbt = entity.getTrackedValue(MEMORIES);
        Map<UUID, Memories> memories = new HashMap<>();
        for (String uuid : nbt.getKeys()) {
            memories.put(UUID.fromString(uuid), Memories.fromCNBT(entity, nbt.getCompound(uuid)));
        }
        return memories;
    }

    public Memories getMemoriesForPlayer(PlayerEntity player) {
        NbtCompound nbt = entity.getTrackedValue(MEMORIES);
        nbt = nbt == null ? new NbtCompound() : nbt;
        NbtCompound compoundTag = nbt.getCompound(player.getUuid().toString());
        Memories returnMemories = Memories.fromCNBT(entity, compoundTag);
        if (returnMemories == null) {
            returnMemories = new Memories(this, player.world.getTimeOfDay(), player.getUuid());
            nbt.put(player.getUuid().toString(), returnMemories.toCNBT());
            entity.setTrackedValue(MEMORIES, nbt);
        }
        return returnMemories;
    }

    public Personality getPersonality() {
        return entity.getTrackedValue(PERSONALITY);
    }

    public Mood getMood() {
        return getPersonality().getMoodGroup().getMood(entity.getTrackedValue(MOOD));
    }

    public boolean isPanicking() {
        return entity.getTrackedValue(PANICKING);
    }

    public void modifyMoodValue(int mood) {
        entity.setTrackedValue(MOOD, MoodGroup.clampMood(this.getMoodValue() + mood));
    }

    public int getMoodValue() {
        return entity.getTrackedValue(MOOD);
    }

    public MoveState getMoveState() {
        return entity.getTrackedValue(MOVE_STATE);
    }

    public void setMoveState(MoveState state, @Nullable PlayerEntity leader) {
        entity.setTrackedValue(MOVE_STATE, state);
        if (state == MoveState.MOVE) {
            entity.getBrain().forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
            entity.getBrain().forget(MemoryModuleTypeMCA.STAYING);
        }
        if (state == MoveState.STAY) {
            entity.getBrain().forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
            entity.getBrain().remember(MemoryModuleTypeMCA.STAYING, true);
        }
        if (state == MoveState.FOLLOW) {
            entity.getBrain().remember(MemoryModuleTypeMCA.PLAYER_FOLLOWING, leader);
            entity.getBrain().forget(MemoryModuleTypeMCA.STAYING);
            abandonJob();
        }
    }

    public void setArmorWear(boolean s) {
        entity.setTrackedValue(WEAR_ARMOR, s);
    }

    public boolean getArmorWear() {
        return entity.getTrackedValue(WEAR_ARMOR);
    }

    /**
     * Read the move state from the active memory.
     */
    public void updateMoveState() {
        if (getMoveState() == MoveState.FOLLOW && !entity.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            if (entity.getBrain().getOptionalMemory(MemoryModuleTypeMCA.STAYING).isPresent()) {
                entity.setTrackedValue(MOVE_STATE, MoveState.STAY);
            } else if (entity.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
                entity.setTrackedValue(MOVE_STATE, MoveState.FOLLOW);
            } else {
                entity.setTrackedValue(MOVE_STATE, MoveState.MOVE);
            }
        }
    }

    public void rewardHearts(ServerPlayerEntity player, int hearts) {
        Memories memory = entity.getVillagerBrain().getMemoriesForPlayer(player);

        if (hearts == 0) {
            return;
        }

        //spawn particles
        if (hearts > 0) {
            entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_POS_INTERACTION);
        } else {
            entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_NEG_INTERACTION);

            //sensitive people doubles the loss
            if (entity.getVillagerBrain().getPersonality() == Personality.SENSITIVE) {
                hearts *= 2;
            }
        }

        memory.modInteractionFatigue(1);
        memory.modHearts(hearts);
        CriterionMCA.HEARTS_CRITERION.trigger(player, memory.getHearts(), hearts, "interaction");
        entity.getVillagerBrain().modifyMoodValue(hearts);
    }
}

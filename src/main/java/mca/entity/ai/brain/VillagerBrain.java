package mca.entity.ai.brain;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ActivityMCA;
import mca.entity.ai.Chore;
import mca.entity.ai.Memories;
import mca.entity.ai.MemoryModuleTypeMCA;
import mca.entity.ai.Mood;
import mca.entity.ai.MoveState;
import mca.entity.ai.relationship.Personality;
import mca.util.network.datasync.CBooleanParameter;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CIntegerParameter;
import mca.util.network.datasync.CTagParameter;
import mca.util.network.datasync.CUUIDParameter;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Util;
import net.minecraft.village.VillagerProfession;

/**
 * Handles memory and complex bodily functions. Such as walking, and not being a nitwit.
 */
public class VillagerBrain {
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME,
            MemoryModuleType.JOB_SITE,
            MemoryModuleType.POTENTIAL_JOB_SITE,
            MemoryModuleType.MEETING_POINT,
            MemoryModuleType.MOBS,
            MemoryModuleType.VISIBLE_MOBS,
            MemoryModuleType.VISIBLE_VILLAGER_BABIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.NEAREST_BED,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.NEAREST_HOSTILE,
            MemoryModuleType.SECONDARY_JOB_SITE,
            MemoryModuleType.HIDING_PLACE,
            MemoryModuleType.HEARD_BELL_TIME,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            MemoryModuleType.LAST_WORKED_AT_POI,
            MemoryModuleType.GOLEM_DETECTED_RECENTLY,
            MemoryModuleTypeMCA.PLAYER_FOLLOWING,
            MemoryModuleTypeMCA.STAYING
    );

    private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_BED,
            SensorType.HURT_BY,
            SensorType.VILLAGER_HOSTILES,
            SensorType.VILLAGER_BABIES,
            SensorType.SECONDARY_POIS,
            SensorType.GOLEM_DETECTED,
            ActivityMCA.EXPLODING_CREEPER
    );

    public static Brain.Profile<VillagerEntityMCA> createProfile() {
        return Brain.createProfile(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static Brain<VillagerEntityMCA> initializeTasks(VillagerEntityMCA villager, Brain<VillagerEntityMCA> brain) {
        VillagerProfession profession = villager.getVillagerData().getProfession();

        if (villager.isBaby()) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.setTaskList(Activity.PLAY, VillagerTasksMCA.getPlayPackage(0.5F));
        } else {
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
            brain.setTaskList(Activity.WORK, VillagerTasksMCA.getWorkPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
        }

        brain.setTaskList(Activity.CORE, VillagerTasksMCA.getCorePackage(profession, 0.5F));
        brain.setTaskList(Activity.MEET, VillagerTasksMCA.getMeetPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.REST, VillagerTasksMCA.getRestPackage(profession, 0.5F));
        brain.setTaskList(Activity.IDLE, VillagerTasksMCA.getIdlePackage(profession, 0.5F));
        brain.setTaskList(Activity.PANIC, VillagerTasksMCA.getPanicPackage(profession, 0.5F));
        brain.setTaskList(Activity.PRE_RAID, VillagerTasksMCA.getPreRaidPackage(profession, 0.5F));
        brain.setTaskList(Activity.RAID, VillagerTasksMCA.getRaidPackage(profession, 0.5F));
        brain.setTaskList(Activity.HIDE, VillagerTasksMCA.getHidePackage(profession, 0.5F));
        brain.setTaskList(ActivityMCA.CHORE, VillagerTasksMCA.getChorePackage(profession, 0.5F));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(villager.world.getTimeOfDay(), villager.world.getTime());

        return brain;
    }

    private final VillagerEntityMCA entity;

    private final CTagParameter memories;
    private final CEnumParameter<Personality> personality;
    private final CIntegerParameter mood;

    public final CEnumParameter<MoveState> moveState;

    private final CEnumParameter<Chore> activeChore;
    private final CUUIDParameter choreAssigningPlayer;

    private final CBooleanParameter panicking;

    public VillagerBrain(VillagerEntityMCA entity, CDataManager data) {
        this.entity = entity;
        memories = data.newTag("memories");
        personality = data.newEnum("personality", Personality.UNASSIGNED);
        mood = data.newInteger("mood");
        moveState = data.newEnum("moveState", MoveState.MOVE);
        activeChore = data.newEnum("activeChore", Chore.NONE);
        panicking = data.newBoolean("isPanicking");
        choreAssigningPlayer = data.newUUID("choreAssigningPlayer");
    }

    public void clientTick() {
        if (entity.world.random.nextBoolean()) {
            if (getMoodLevel() <= -15) {
                getPersonality().getMoodGroup().getParticles().ifPresent(entity::produceParticles);
            } else if (getMoodLevel() >= 15) {
                entity.produceParticles(ParticleTypes.HAPPY_VILLAGER);
            }
        }
    }

    public void think() {
        // When you relog, it should continue doing the chores.
        // Chore saves but Activity doesn't, so this checks if the activity is not on there and puts it on there.

        if (activeChore.get() != Chore.NONE) {
            // find something to do
            entity.getBrain().getFirstPossibleNonCoreActivity().ifPresent(activity -> {
                if (!activity.equals(ActivityMCA.CHORE)) {
                    entity.getBrain().doExclusively(ActivityMCA.CHORE);
                }
            });
        }

        boolean panicking = entity.getBrain().hasActivity(Activity.PANIC);
        if (panicking != this.panicking.get()) {
            this.panicking.set(panicking);
        }

        if (entity.age % 20 != 0) {
            updateMoveState();
        }
    }

    public Chore getCurrentJob() {
        return activeChore.get();
    }

    public Optional<PlayerEntity> getJobAssigner() {
        return this.choreAssigningPlayer.get().map(id -> entity.world.getPlayerByUuid(id));
    }

    /**
     * Tells the villager to stop doing whatever it's doing.
     */
    public void abandonJob() {
        entity.getBrain().doExclusively(Activity.IDLE);
        activeChore.set(Chore.NONE);
        choreAssigningPlayer.set(Util.NIL_UUID);
    }

    /**
     * Assigns a job for the villager to do.
     */
    public void assignJob(Chore chore, PlayerEntity player) {
        entity.getBrain().doExclusively(ActivityMCA.CHORE);
        activeChore.set(chore);
        choreAssigningPlayer.set(player.getUuid());
        entity.getBrain().forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
        entity.getBrain().forget(MemoryModuleTypeMCA.STAYING);
    }

    public void randomize() {
        personality.set(Personality.getRandom());
        //since minLevel is -100 and it makes no
        mood.set(Mood.getLevel(entity.world.random.nextInt(Mood.maxLevel - Mood.normalMinLevel + 1) + Mood.normalMinLevel));
    }

    public void updateMemories(Memories memories) {
        NbtCompound nbt = this.memories.get();
        nbt.put(memories.getPlayerUUID().toString(), memories.toCNBT());
        this.memories.set(nbt);
    }

    public Memories getMemoriesForPlayer(PlayerEntity player) {
        NbtCompound nbt = memories.get();
        NbtCompound compoundTag = nbt.getCompound(player.getUuid().toString());
        Memories returnMemories = Memories.fromCNBT(entity, compoundTag);
        if (returnMemories == null) {
            returnMemories = new Memories(this, player.world.getTimeOfDay(), player.getUuid());
            nbt.put(player.getUuid().toString(), returnMemories.toCNBT());
            memories.set(nbt);
        }
        return returnMemories;
    }

    public Personality getPersonality() {
        return personality.get();
    }

    public Mood getMood() {
        return getPersonality().getMoodGroup().getMood(mood.get());
    }

    public boolean isPanicking() {
        return panicking.get();
    }

    public void modifyMoodLevel(int mood) {
        this.mood.set(Mood.getLevel(this.getMoodLevel() + mood));
    }

    public int getMoodLevel() {
        return mood.get();
    }

    public MoveState getMoveState() {
        return moveState.get();
    }

    public void setMoveState(MoveState state, @Nullable PlayerEntity leader) {
        moveState.set(state);
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

    /**
     * Read the move state from the active memory.
     */
    public void updateMoveState() {
        if (getMoveState() == MoveState.FOLLOW && !entity.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            if (entity.getBrain().getOptionalMemory(MemoryModuleTypeMCA.STAYING).isPresent()) {
                moveState.set(MoveState.STAY);
            } else if (entity.getBrain().getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
                moveState.set(MoveState.FOLLOW);
            } else {
                moveState.set(MoveState.MOVE);
            }
        }
    }

}

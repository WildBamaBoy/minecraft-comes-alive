package mca.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import mca.entity.EntitiesMCA;
import mca.entity.EquipmentSet;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ActivityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.SchedulesMCA;
import mca.entity.ai.brain.tasks.BowTask;
import mca.entity.ai.brain.tasks.EnterFavoredBuildingTask;
import mca.entity.ai.brain.tasks.EquipmentTask;
import mca.entity.ai.brain.tasks.ExtendedMeleeAttackTask;
import mca.entity.ai.brain.tasks.ExtendedSleepTask;
import mca.entity.ai.brain.tasks.FollowTask;
import mca.entity.ai.brain.tasks.GreetPlayerTask;
import mca.entity.ai.brain.tasks.InteractTask;
import mca.entity.ai.brain.tasks.LoseUnimportantJobTask;
import mca.entity.ai.brain.tasks.PatrolVillageTask;
import mca.entity.ai.brain.tasks.ShoutTask;
import mca.entity.ai.brain.tasks.StayTask;
import mca.entity.ai.brain.tasks.WanderOrTeleportToTargetTask;
import mca.entity.ai.brain.tasks.chore.ChoppingTask;
import mca.entity.ai.brain.tasks.chore.FishingTask;
import mca.entity.ai.brain.tasks.chore.HarvestingTask;
import mca.entity.ai.brain.tasks.chore.HuntingTask;
import mca.entity.ai.relationship.AgeState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class VillagerTasksMCA {
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
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
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleTypeMCA.PLAYER_FOLLOWING,
            MemoryModuleTypeMCA.STAYING,
            MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY,
            MemoryModuleTypeMCA.WEARS_ARMOR,
            MemoryModuleTypeMCA.SMALL_BOUNTY
    );

    public static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_BED,
            SensorType.HURT_BY,
            SensorType.VILLAGER_HOSTILES,
            SensorType.SECONDARY_POIS,
            SensorType.GOLEM_DETECTED,
            ActivityMCA.VILLAGER_BABIES,
            ActivityMCA.EXPLODING_CREEPER,
            ActivityMCA.GUARD_ENEMIES
    );

    public static Brain.Profile<VillagerEntityMCA> createProfile() {
        return Brain.createProfile(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static Brain<VillagerEntityMCA> initializeTasks(VillagerEntityMCA villager, Brain<VillagerEntityMCA> brain) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        AgeState age = AgeState.byCurrentAge(villager.getBreedingAge());

        if (age == AgeState.BABY) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            //todo babies may get a little bit more AI
            return brain;
        } else if (age != AgeState.ADULT) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.setTaskList(Activity.PLAY, VillagerTasksMCA.getPlayPackage(0.5F));
            brain.setTaskList(Activity.CORE, VillagerTasksMCA.getSelfDefencePackage(profession, 0.5f));
        } else if (villager.isGuard()) {
            brain.setSchedule(villager.getRandom().nextBoolean() ? SchedulesMCA.GUARD : SchedulesMCA.GUARD_NIGHT);
            brain.setTaskList(Activity.CORE, VillagerTasksMCA.getGuardCorePackage(villager));
            brain.setTaskList(Activity.WORK, VillagerTasksMCA.getGuardWorkPackage(villager));
            brain.setTaskList(Activity.PANIC, VillagerTasksMCA.getGuardPanicPackage(0.5f));
            brain.setTaskList(Activity.RAID, VillagerTasksMCA.getGuardWorkPackage(villager));
        } else if (profession == ProfessionsMCA.OUTLAW) {
            brain.setSchedule(SchedulesMCA.DEFAULT);
            // todo how do villager behave when they are on death row?
        } else {
            brain.setSchedule(SchedulesMCA.DEFAULT);
            brain.setTaskList(Activity.WORK, VillagerTasksMCA.getWorkPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
            brain.setTaskList(Activity.CORE, VillagerTasksMCA.getSelfDefencePackage(profession, 0.5f));
            brain.setTaskList(Activity.RAID, VillagerTasksMCA.getRaidPackage(profession, 0.5F));
        }

        brain.setTaskList(Activity.CORE, VillagerTasksMCA.getCorePackage(profession, 0.5F));
        brain.setTaskList(Activity.MEET, VillagerTasksMCA.getMeetPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.REST, VillagerTasksMCA.getRestPackage(profession, 0.5F));
        brain.setTaskList(Activity.IDLE, VillagerTasksMCA.getIdlePackage(profession, 0.5F));
        brain.setTaskList(Activity.PANIC, VillagerTasksMCA.getPanicPackage(profession, 0.5F));
        brain.setTaskList(Activity.PRE_RAID, VillagerTasksMCA.getPreRaidPackage(profession, 0.5F));
        brain.setTaskList(Activity.HIDE, VillagerTasksMCA.getHidePackage(profession, 0.5F));
        brain.setTaskList(ActivityMCA.CHORE, VillagerTasksMCA.getChorePackage(profession, 0.5F));
        brain.setTaskList(ActivityMCA.GRIEVE, VillagerTasksMCA.getGrievingPackage());

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(villager.world.getTimeOfDay(), villager.world.getTime());

        return brain;
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getCorePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new FollowTask()),
                Pair.of(0, new StayTask()),
                Pair.of(0, new GreetPlayerTask()),
                Pair.of(0, new StayAboveWaterTask(0.8F)),
                Pair.of(0, new OpenDoorsTask()),
                Pair.of(0, new LookAroundTask(45, 90)),
                Pair.of(0, new WakeUpTask()),
                Pair.of(0, new HideWhenBellRingsTask()),
                Pair.of(0, new StartRaidTask()),
                Pair.of(0, new ForgetCompletedPointOfInterestTask(profession.getWorkStation(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ForgetCompletedPointOfInterestTask(profession.getWorkStation(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new WanderOrTeleportToTargetTask()),
                Pair.of(2, new WorkStationCompetitionTask(profession)),
                Pair.of(3, new InteractTask(speedModifier)),
                Pair.of(3, new FollowCustomerTask(speedModifier)),
                Pair.of(5, new WalkToNearestVisibleWantedItemTask<>(speedModifier, false, 4)),
                Pair.of(6, new FindPointOfInterestTask(profession.getWorkStation(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new WalkTowardJobSiteTask(speedModifier)),
                Pair.of(8, new TakeJobSiteTask(speedModifier)),
                Pair.of(10, new FindPointOfInterestTask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
                Pair.of(10, new GoToWorkTask()),
                Pair.of(10, new LoseUnimportantJobTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getSelfDefencePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new PanicTask()),
                Pair.of(1, new EquipmentTask(VillagerTasksMCA::isInDanger, v -> EquipmentSet.NAKED)),
                Pair.of(2, new ExtendedMeleeAttackTask(15, 2.5F, MemoryModuleType.NEAREST_HOSTILE))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getGuardCorePackage(VillagerEntityMCA villager) {
        return ImmutableList.of(
                Pair.of(0, new ConditionalTask<>(VillagerTasksMCA::guardTooHurt,
                        new PanicTask()
                )),
                Pair.of(0,
                        new ShoutTask("villager.retreat", 100, e -> VillagerTasksMCA.guardTooHurt(e) && e.getVillagerBrain().isPanicking())
                ),
                Pair.of(0,
                        new ShoutTask("villager.attack", 160, e -> !VillagerTasksMCA.guardTooHurt(e) && VillagerTasksMCA.getPreferredTarget(e).isPresent())
                ),
                Pair.of(0, new ConditionalTask<>(VillagerTasksMCA::guardTooHurt,
                        // self-defence while fleeing
                        new ExtendedMeleeAttackTask(15, 2.5F, MemoryModuleType.NEAREST_HOSTILE)
                )),
                Pair.of(1, new EquipmentTask(VillagerTasksMCA::isOnDuty, v -> v.getResidency().getHomeVillage()
                        .map(vil -> vil.getGuardEquipment(v.getProfession())).orElse(EquipmentSet.GUARD_0))),
                Pair.of(2, new UpdateAttackTargetTask<>(t -> true, VillagerTasksMCA::getPreferredTarget)),
                Pair.of(3, new ForgetAttackTargetTask<>(livingEntity -> !VillagerTasksMCA.isPreferredTarget(villager, livingEntity))),
                Pair.of(4, new BowTask<>(20, 12)),
                Pair.of(5, new ConditionalTask<>(v -> v.isHolding(Items.CROSSBOW),
                        new AttackTask<>(5, 0.75F)
                )),
                Pair.of(6, new RangedApproachTask(0.75F)),
                Pair.of(7, new ExtendedMeleeAttackTask(20, 2.0F)),
                Pair.of(8, new CrossbowAttackTask<VillagerEntityMCA, VillagerEntityMCA>())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getGuardWorkPackage(VillagerEntityMCA villager) {
        return ImmutableList.of(
                Pair.of(10, new PatrolVillageTask(4, 0.4f)),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getGuardPanicPackage(float speedModifier) {
        float f = speedModifier * 1.5F;
        return ImmutableList.of(
                Pair.of(1, new StopPanickingTask()),
                Pair.of(2, GoToRememberedPositionTask.toEntity(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)),
                Pair.of(2, GoToRememberedPositionTask.toEntity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)),
                Pair.of(3, new FindWalkTargetTask(f, 2, 2)),
                getMinimalLookBehavior()
        );
    }

    private static boolean guardTooHurt(VillagerEntityMCA villager) {
        return villager.getHealth() < villager.getMaxHealth() * 0.25;
    }

    private static Optional<? extends LivingEntity> getPreferredTarget(VillagerEntityMCA villager) {
        if (guardTooHurt(villager)) {
            return Optional.empty();
        } else {
            Optional<LivingEntity> primary = villager.getBrain().getOptionalMemory(MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY);
            if (primary.isPresent() && (getActivity(villager) != Activity.REST || primary.get().distanceTo(villager) < 8.0)) {
                return primary;
            } else {
                return villager.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
            }
        }
    }

    private static boolean isPreferredTarget(VillagerEntityMCA villager, LivingEntity entity) {
        Optional<? extends LivingEntity> target = getPreferredTarget(villager);
        return target.filter(livingEntity -> livingEntity == entity).isPresent();
    }

    public static boolean isOnDuty(VillagerEntityMCA villager) {
        return getActivity(villager) == Activity.WORK || villager.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
    }

    public static boolean isInDanger(VillagerEntityMCA villager) {
        return villager.getVillagerBrain().isPanicking()
                || villager.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
    }

    private static Activity getActivity(VillagerEntityMCA villager) {
        return villager.getBrain().getSchedule().getActivityForTime((int)(villager.world.getTimeOfDay() % 24000L));
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getGrievingPackage() {
        return ImmutableList.of(
                Pair.of(0, new CompositeTask<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                        CompositeTask.Order.ORDERED,
                        CompositeTask.RunMode.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new FindWalkTargetTask(1.5F), 1),
                                Pair.of(new WaitTask(80, 180), 2)
                        )
                )),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getWorkPackage(VillagerProfession profession, float speedModifier) {
        VillagerWorkTask spawngolemtask;
        if (profession == VillagerProfession.FARMER) {
            spawngolemtask = new FarmerWorkTask();
        } else {
            spawngolemtask = new VillagerWorkTask();
        }

        return ImmutableList.of(
                getMinimalLookBehavior(),
                Pair.of(5, new RandomTask<>(
                        ImmutableList.of(Pair.of(spawngolemtask, 7),
                                Pair.of(new GoToIfNearbyTask(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                                Pair.of(new GoToNearbyPositionTask(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                                Pair.of(new GoToSecondaryPositionTask(MemoryModuleType.SECONDARY_JOB_SITE, speedModifier, 1, 6, MemoryModuleType.JOB_SITE), 5),
                                Pair.of(new FarmerVillagerTask(), profession == VillagerProfession.FARMER ? 2 : 5),
                                Pair.of(new BoneMealTask(), profession == VillagerProfession.FARMER ? 4 : 7))
                )),
                Pair.of(10, new HoldTradeOffersTask(400, 1600)),
                Pair.of(10, new FindInteractionTargetTask(EntityType.PLAYER, 4)),
                Pair.of(2, new VillagerWalkTowardsTask(MemoryModuleType.JOB_SITE, speedModifier, 9, 100, 1200)),
                Pair.of(3, new GiveGiftsToHeroTask(100)),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getPlayPackage(float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new WanderAroundTask(80, 120)),
                getFullLookBehavior(),
                Pair.of(5, new PlayWithVillagerBabiesTask()),
                Pair.of(5, new RandomTask<>(
                        ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleState.VALUE_ABSENT),
                        ImmutableList.of(
                                Pair.of(FindEntityTask.create(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 2),
                                Pair.of(FindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 1),
                                Pair.of(new FindWalkTargetTask(speedModifier), 1),
                                Pair.of(new GoTowardsLookTarget(speedModifier, 2), 1),
                                Pair.of(new JumpInBedTask(speedModifier), 2),
                                Pair.of(new WaitTask(20, 40), 2)
                        ))),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getRestPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(3, new ExtendedSleepTask(speedModifier)),
                Pair.of(5, new RandomTask<>( //behavior when they are homeless
                        ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_ABSENT),
                        ImmutableList.of(
                                Pair.of(new WalkHomeTask(speedModifier), 1),
                                Pair.of(new WanderIndoorsTask(speedModifier), 4),
                                Pair.of(new GoToPointOfInterestTask(speedModifier, 4), 2),
                                Pair.of(new WaitTask(20, 40), 2))
                )),
                getMinimalLookBehavior(),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getMeetPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(2, new RandomTask<>(ImmutableList.of(
                        Pair.of(new GoToIfNearbyTask(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2),
                        Pair.of(new MeetVillagerTask(), 2))
                )),
                Pair.of(10, new HoldTradeOffersTask(400, 1600)),
                Pair.of(10, new FindInteractionTargetTask(EntityType.PLAYER, 4)),
                Pair.of(2, new VillagerWalkTowardsTask(MemoryModuleType.MEETING_POINT, speedModifier, 6, 100, 200)),
                Pair.of(3, new GiveGiftsToHeroTask(100)),
                Pair.of(3, new ForgetCompletedPointOfInterestTask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT)),
                Pair.of(3, new CompositeTask<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                        CompositeTask.Order.ORDERED,
                        CompositeTask.RunMode.RUN_ONE,
                        ImmutableList.of(Pair.of(new GatherItemsVillagerTask(), 1)) // GOSSIP TASK
                )),
                getFullLookBehavior(),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getIdlePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(1, new EnterFavoredBuildingTask(0.5f)),
                Pair.of(2, new RandomTask<>(ImmutableList.of(
                        Pair.of(FindEntityTask.create(EntitiesMCA.FEMALE_VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 2),
                        Pair.of(FindEntityTask.create(EntitiesMCA.MALE_VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 2),
                        Pair.of(FindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 1),
                        Pair.of(new FindWalkTargetTask(speedModifier), 1),
                        Pair.of(new GoTowardsLookTarget(speedModifier, 2), 1),
                        Pair.of(new JumpInBedTask(speedModifier), 1),
                        Pair.of(new WaitTask(30, 60), 1))
                )),
                Pair.of(3, new GiveGiftsToHeroTask(100)),
                Pair.of(3, new FindInteractionTargetTask(EntityType.PLAYER, 4)),
                Pair.of(3, new HoldTradeOffersTask(400, 1600)),
                Pair.of(3, new CompositeTask<>(ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                        CompositeTask.Order.ORDERED,
                        CompositeTask.RunMode.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new GatherItemsVillagerTask(), 1))
                )),
                /*
                Pair.of(3, new CompositeTask<>(ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.BREED_TARGET),
                        CompositeTask.Order.ORDERED,
                        CompositeTask.RunMode.RUN_ONE,
                        ImmutableList.of(Pair.of(new VillagerBreedTask(), 1))
                )),
                */
                getFullLookBehavior(),
                Pair.of(99, new ScheduleActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getPanicPackage(VillagerProfession profession, float speedModifier) {
        float f = speedModifier * 1.5F;
        return ImmutableList.of(
                Pair.of(0, new StopPanickingTask()),
                Pair.of(1, GoToRememberedPositionTask.toEntity(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)),
                Pair.of(1, GoToRememberedPositionTask.toEntity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)),
                Pair.of(3, new FindWalkTargetTask(f, 2, 2)),
                getMinimalLookBehavior()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getPreRaidPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new RingBellTask()),
                Pair.of(0, new RandomTask<>(ImmutableList.of(
                        Pair.of(new VillagerWalkTowardsTask(MemoryModuleType.MEETING_POINT, speedModifier * 1.5F, 2, 150, 200), 6),
                        Pair.of(new FindWalkTargetTask(speedModifier * 1.5F), 2))
                )),
                getMinimalLookBehavior(),
                Pair.of(99, new EndRaidTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getRaidPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new RandomTask<>(ImmutableList.of(
                        Pair.of(new SeekSkyAfterRaidWinTask(speedModifier), 5),
                        Pair.of(new RunAroundAfterRaidTask(speedModifier * 1.1F), 2)
                ))),
                Pair.of(0, new CelebrateRaidWinTask(600, 600)),
                Pair.of(2, new HideInHomeDuringRaidTask(24, speedModifier * 1.4F)),
                getMinimalLookBehavior(),
                Pair.of(99, new EndRaidTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getHidePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new ForgetBellRingTask(15, 3)),
                Pair.of(1, new HideInHomeTask(32, speedModifier * 1.25F, 2)),
                getMinimalLookBehavior()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getChorePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new ChoppingTask()),
                Pair.of(0, new FishingTask()),
                Pair.of(0, new HarvestingTask()),
                Pair.of(0, new HuntingTask())
        );
    }

    private static Pair<Integer, Task<LivingEntity>> getFullLookBehavior() {
        return Pair.of(5, new RandomTask<>(ImmutableList.of(
                Pair.of(new FollowMobTask(EntityType.CAT, 8.0F), 8),
                Pair.of(new FollowMobTask(EntityType.VILLAGER, 8.0F), 2),
                Pair.of(new FollowMobTask(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new FollowMobTask(SpawnGroup.CREATURE, 8.0F), 1),
                Pair.of(new FollowMobTask(SpawnGroup.WATER_CREATURE, 8.0F), 1),
                Pair.of(new FollowMobTask(SpawnGroup.WATER_AMBIENT, 8.0F), 1),
                Pair.of(new FollowMobTask(SpawnGroup.MONSTER, 8.0F), 1),
                Pair.of(new WaitTask(30, 60), 2)))
        );
    }

    private static Pair<Integer, Task<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(5, new RandomTask<>(ImmutableList.of(
                Pair.of(new FollowMobTask(EntityType.VILLAGER, 8.0F), 2),
                Pair.of(new FollowMobTask(EntityType.PLAYER, 8.0F), 2),
                Pair.of(new WaitTask(30, 60), 8)))
        );
    }
}

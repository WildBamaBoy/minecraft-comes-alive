package mca.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.brain.tasks.*;
import mca.entity.ai.brain.tasks.chore.ChoppingTask;
import mca.entity.ai.brain.tasks.chore.FishingTask;
import mca.entity.ai.brain.tasks.chore.HarvestingTask;
import mca.entity.ai.brain.tasks.chore.HuntingTask;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;

import java.util.Optional;

public class VillagerTasksMCA {
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getCorePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new FollowTask()),
                Pair.of(0, new StayTask()),
                Pair.of(0, new ProcreateTask()),
                Pair.of(0, new SwimTask(0.8F)),
                Pair.of(0, new InteractWithDoorTask()),
                Pair.of(0, new LookTask(45, 90)),
                Pair.of(0, new PanicTask()),
                Pair.of(0, new WakeUpTask()),
                Pair.of(0, new HideFromRaidOnBellRingTask()),
                Pair.of(0, new BeginRaidTask()),
                Pair.of(0, new ExpirePOITask(profession.getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ExpirePOITask(profession.getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new WalkOrTeleportToTargetTask()),
                Pair.of(2, new SwitchVillagerJobTask(profession)),
                Pair.of(3, new InteractTask(speedModifier)),
                Pair.of(3, new TradeTask(speedModifier)),
                Pair.of(5, new PickupWantedItemTask<>(speedModifier, false, 4)),
                Pair.of(6, new GatherPOITask(profession.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new FindPotentialJobTask(speedModifier)),
                Pair.of(8, new FindJobTask(speedModifier)),
                Pair.of(10, new GatherPOITask(PointOfInterestType.HOME, MemoryModuleType.HOME, false, Optional.of((byte) 14))),
                Pair.of(10, new GatherPOITask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte) 14))),
                Pair.of(10, new ExtendedAssignProfessionTask()),
                Pair.of(10, new ExtendedChangeJobTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getWorkPackage(VillagerProfession profession, float speedModifier) {
        SpawnGolemTask spawngolemtask;
        if (profession == VillagerProfession.FARMER) {
            spawngolemtask = new FarmerWorkTask();
        } else {
            spawngolemtask = new SpawnGolemTask();
        }

        return ImmutableList.of(
                getMinimalLookBehavior(),
                Pair.of(5, new FirstShuffledTask<>(
                        ImmutableList.of(Pair.of(spawngolemtask, 7),
                                Pair.of(new WorkTask(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                                Pair.of(new WalkTowardsPosTask(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                                Pair.of(new WalkTowardsRandomSecondaryPosTask(MemoryModuleType.SECONDARY_JOB_SITE, speedModifier, 1, 6, MemoryModuleType.JOB_SITE), 5),
                                Pair.of(new FarmTask(), profession == VillagerProfession.FARMER ? 2 : 5),
                                Pair.of(new BoneMealCropsTask(), profession == VillagerProfession.FARMER ? 4 : 7))
                )),
                Pair.of(10, new ShowWaresTask(400, 1600)),
                Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
                Pair.of(2, new StayNearPointTask(MemoryModuleType.JOB_SITE, speedModifier, 9, 100, 1200)),
                Pair.of(3, new GiveHeroGiftsTask(100)),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getPlayPackage(float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new WalkToTargetTask(80, 120)),
                getFullLookBehavior(),
                Pair.of(5, new WalkToVillagerBabiesTask()),
                Pair.of(5, new FirstShuffledTask<>(
                        ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleStatus.VALUE_ABSENT),
                        ImmutableList.of(
                                Pair.of(InteractWithEntityTask.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 2),
                                Pair.of(InteractWithEntityTask.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 1),
                                Pair.of(new FindWalkTargetTask(speedModifier), 1),
                                Pair.of(new WalkTowardsLookTargetTask(speedModifier, 2), 1),
                                Pair.of(new JumpOnBedTask(speedModifier), 2),
                                Pair.of(new DummyTask(20, 40), 2)
                        ))),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getRestPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(2, new StayNearPointTask(MemoryModuleType.HOME, speedModifier, 1, 150, 1200)),
                Pair.of(3, new SleepAtHomeTask()),
                Pair.of(5, new FirstShuffledTask<>(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleStatus.VALUE_ABSENT),
                        ImmutableList.of(
                                Pair.of(new WalkToHouseTask(speedModifier), 1),
                                Pair.of(new WalkRandomlyInsideTask(speedModifier), 4),
                                Pair.of(new WalkToPOITask(speedModifier, 4), 2),
                                Pair.of(new DummyTask(20, 40), 2))
                )),
                getMinimalLookBehavior(),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getMeetPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(2, new FirstShuffledTask<>(
                        ImmutableList.of(
                                Pair.of(new WorkTask(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2),
                                Pair.of(new CongregateTask(), 2))
                )),
                Pair.of(10, new ShowWaresTask(400, 1600)),
                Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
                Pair.of(2, new StayNearPointTask(MemoryModuleType.MEETING_POINT, speedModifier, 6, 100, 200)),
                Pair.of(3, new GiveHeroGiftsTask(100)),
                Pair.of(3, new ExpirePOITask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT)),
                Pair.of(3, new MultiTask<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                        MultiTask.Ordering.ORDERED,
                        MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new ShareItemsTask(), 1)) // GOSSIP TASK
                )),
                getFullLookBehavior(),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getIdlePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(2, new FirstShuffledTask<>(
                        ImmutableList.of(
                                Pair.of(InteractWithEntityTask.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 2),
                                Pair.of(new InteractWithEntityTask<>(EntityType.VILLAGER, 8, AgeableEntity::canBreed, AgeableEntity::canBreed, MemoryModuleType.BREED_TARGET, speedModifier, 2), 1),
                                Pair.of(InteractWithEntityTask.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speedModifier, 2), 1),
                                Pair.of(new FindWalkTargetTask(speedModifier), 1), Pair.of(new WalkTowardsLookTargetTask(speedModifier, 2), 1),
                                Pair.of(new JumpOnBedTask(speedModifier), 1), Pair.of(new DummyTask(30, 60), 1))
                )),
                Pair.of(3, new GiveHeroGiftsTask(100)),
                Pair.of(3, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
                Pair.of(3, new ShowWaresTask(400, 1600)),
                Pair.of(3, new MultiTask<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                        MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new ShareItemsTask(), 1))
                )),
                Pair.of(3, new MultiTask<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.BREED_TARGET),
                        MultiTask.Ordering.ORDERED,
                        MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new CreateBabyVillagerTask(), 1))
                )),
                getFullLookBehavior(),
                Pair.of(99, new UpdateActivityTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getPanicPackage(VillagerProfession profession, float speedModifier) {
        float f = speedModifier * 1.5F;
        return ImmutableList.of(
                Pair.of(0, new ClearHurtTask()),
                Pair.of(1, RunAwayTask.entity(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)),
                Pair.of(1, RunAwayTask.entity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)),
                Pair.of(3, new FindWalkTargetTask(f, 2, 2)),
                getMinimalLookBehavior()
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getPreRaidPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new RingBellTask()),
                Pair.of(0, new FirstShuffledTask<>(
                        ImmutableList.of(
                                Pair.of(new StayNearPointTask(MemoryModuleType.MEETING_POINT, speedModifier * 1.5F, 2, 150, 200), 6),
                                Pair.of(new FindWalkTargetTask(speedModifier * 1.5F), 2))
                )),
                getMinimalLookBehavior(),
                Pair.of(99, new ForgetRaidTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getRaidPackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new FirstShuffledTask<>(
                        ImmutableList.of(
                                Pair.of(new GoOutsideAfterRaidTask(speedModifier), 5),
                                Pair.of(new FindWalkTargetAfterRaidVictoryTask(speedModifier * 1.1F), 2)
                        ))),
                Pair.of(0, new CelebrateRaidVictoryTask(600, 600)),
                Pair.of(2, new FindHidingPlaceDuringRaidTask(24, speedModifier * 1.4F)),
                getMinimalLookBehavior(),
                Pair.of(99, new ForgetRaidTask())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntityMCA>>> getHidePackage(VillagerProfession profession, float speedModifier) {
        return ImmutableList.of(
                Pair.of(0, new ExpireHidingTask(15, 3)),
                Pair.of(1, new FindHidingPlaceTask(32, speedModifier * 1.25F, 2)),
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
        return Pair.of(5, new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.CAT, 8.0F), 8), Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2), Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2), Pair.of(new LookAtEntityTask(EntityClassification.CREATURE, 8.0F), 1), Pair.of(new LookAtEntityTask(EntityClassification.WATER_CREATURE, 8.0F), 1), Pair.of(new LookAtEntityTask(EntityClassification.WATER_AMBIENT, 8.0F), 1), Pair.of(new LookAtEntityTask(EntityClassification.MONSTER, 8.0F), 1), Pair.of(new DummyTask(30, 60), 2))));
    }

    private static Pair<Integer, Task<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(5, new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2), Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2), Pair.of(new DummyTask(30, 60), 8))));
    }
}
package mca.entity.ai;

import mca.TagsMCA;
import mca.advancement.criterion.CriterionMCA;
import mca.block.TombstoneBlock;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.*;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.entity.interaction.gifts.GiftSaturation;
import mca.server.world.data.GraveyardManager;
import mca.server.world.data.GraveyardManager.TombstoneState;
import mca.util.WorldUtils;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * I know you, you know me, we're all a big happy family.
 */
public class Relationship<T extends MobEntity & VillagerLike<T>> implements EntityRelationship {
    public static final Predicate IS_MARRIED = (villager, player) -> villager.getRelationships().isMarriedTo(player);
    public static final Predicate IS_RELATIVE = (villager, player) -> villager.getRelationships().getFamilyEntry().isRelative(player);
    public static final Predicate IS_FAMILY = IS_MARRIED.or(IS_RELATIVE);
    public static final Predicate IS_PARENT = (villager, player) -> villager.getRelationships().getFamilyEntry().isParent(player);
    public static final Predicate IS_ORPHAN = (villager, player) -> villager.getRelationships().getFamilyEntry().getParents().allMatch(FamilyTreeNode::isDeceased);

    private static final CDataParameter<String> SPOUSE_NAME = CParameter.create("spouseName", "");
    private static final CDataParameter<Optional<UUID>> SPOUSE_UUID = CParameter.create("spouseUUID", Optional.empty());
    private static final CEnumParameter<MarriageState> MARRIAGE_STATE = CParameter.create("marriageState", MarriageState.SINGLE);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder.addAll(SPOUSE_NAME, SPOUSE_UUID, MARRIAGE_STATE);
    }

    protected final T entity;

    private final GiftSaturation giftSaturation = new GiftSaturation();

    public Relationship(T entity) {
        this.entity = entity;
    }

    @Override
    public Gender getGender() {
        return entity.getGenetics().getGender();
    }

    @Override
    public Optional<Text> getSpouseName() {
        return isMarried() ? Optional.ofNullable(entity.getTrackedValue(SPOUSE_NAME)).map(LiteralText::new) : Optional.empty();
    }

    @Override
    public Optional<Entity> getSpouse() {
        return entity.getTrackedValue(SPOUSE_UUID).map(id -> ((ServerWorld)entity.world).getEntity(id));
    }

    @Override
    public FamilyTree getFamilyTree() {
        return FamilyTree.get((ServerWorld)entity.world);
    }

    @NotNull
    @Override
    public FamilyTreeNode getFamilyEntry() {
        return getFamilyTree().getOrCreate(entity);
    }

    @Override
    public Stream<Entity> getFamily(int parents, int children) {
        return getFamilyEntry()
                .getRelatives(parents, children)
                .map(id -> ((ServerWorld)entity.world).getEntity(id))
                .filter(Objects::nonNull)
                .filter(e -> !e.equals(entity));
    }

    @Override
    public Stream<Entity> getParents() {
        return getFamilyEntry().parents().map(((ServerWorld)entity.world)::getEntity).filter(Objects::nonNull);
    }

    @Override
    public Stream<Entity> getSiblings() {
        return getFamilyEntry()
                .siblings()
                .stream()
                .map(id -> ((ServerWorld)entity.world).getEntity(id))
                .filter(Objects::nonNull)
                .filter(e -> !e.equals(entity)); // we exclude ourselves from the list of siblings
    }

    public boolean onDeath(DamageSource cause) {
        getFamilyEntry().setDeceased(true);
        return GraveyardManager.get((ServerWorld)entity.world).findNearest(entity.getBlockPos(), TombstoneState.EMPTY, 7).filter(pos -> {
            if (entity.world.getBlockState(pos).isIn(TagsMCA.Blocks.TOMBSTONES)) {
                BlockEntity be = entity.world.getBlockEntity(pos);
                if (be instanceof TombstoneBlock.Data) {
                    ((TombstoneBlock.Data)be).setEntity(entity);

                    onTragedy(cause, pos);
                    return true;
                }
            }
            return false;
        }).isPresent();
    }

    public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite) {
        // The death of a villager negatively modifies the mood of nearby strangers
        if (!entity.isHostile()) {
            WorldUtils
                    .getCloseEntities(entity.world, entity, 32, VillagerEntityMCA.class)
                    .forEach(villager -> villager.getRelationships().onTragedy(cause, burialSite, RelationshipType.STRANGER));
        }

        onTragedy(cause, burialSite, RelationshipType.SELF);
    }

    @Override
    public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type) {
        int moodAffect = 10;

        moodAffect /= type.getInverseProximity();
        moodAffect *= type.getProximityAmplifier();

        entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_TRAGEDY);
        entity.getVillagerBrain().modifyMoodValue(-moodAffect);

        // seen murder
        if (cause.getAttacker() instanceof PlayerEntity) {
            entity.getVillagerBrain().getMemoriesForPlayer((PlayerEntity)cause.getAttacker()).modHearts(-20);
        }

        if (burialSite != null && type != RelationshipType.STRANGER) {
            entity.getBrain().doExclusively(ActivityMCA.GRIEVE);
            entity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(burialSite, 1, 1));
            entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(burialSite));
        }
        EntityRelationship.super.onTragedy(cause, burialSite, type);
    }

    @Override
    public MarriageState getMarriageState() {
        return entity.getTrackedValue(MARRIAGE_STATE);
    }

    @Override
    public Optional<UUID> getSpouseUuid() {
        return entity.getTrackedValue(SPOUSE_UUID);
    }

    @Override
    public void marry(Entity spouse) {
        entity.setTrackedValue(SPOUSE_UUID, Optional.of(spouse.getUuid()));
        entity.setTrackedValue(SPOUSE_NAME, spouse.getName().getString());

        MarriageState state = spouse instanceof PlayerEntity ? MarriageState.MARRIED_TO_PLAYER : MarriageState.MARRIED_TO_VILLAGER;

        if (spouse instanceof ServerPlayerEntity) {
            CriterionMCA.GENERIC_EVENT_CRITERION.trigger((ServerPlayerEntity)spouse, "marriage");
        }

        entity.setTrackedValue(MARRIAGE_STATE, state);
        getFamilyEntry().updateMarriage(spouse, state);
    }

    @Override
    public void endMarriage(MarriageState newState) {
        entity.setTrackedValue(SPOUSE_UUID, Optional.empty());
        entity.setTrackedValue(SPOUSE_NAME, "");
        entity.setTrackedValue(MARRIAGE_STATE, newState);
        getFamilyEntry().setMarriageState(newState);
    }

    public GiftSaturation getGiftSaturation() {
        return giftSaturation;
    }

    public void readFromNbt(NbtCompound nbt) {
        giftSaturation.readFromNbt(nbt.getList("giftSaturationQueue", 8));
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.put("giftSaturationQueue", giftSaturation.toNbt());
    }

    public interface Predicate extends BiPredicate<CompassionateEntity<?>, Entity> {

        boolean test(CompassionateEntity<?> villager, UUID partner);

        @Override
        default boolean test(CompassionateEntity<?> villager, Entity partner) {
            return partner != null && test(villager, partner.getUuid());
        }

        default Predicate or(Predicate b) {
            return (villager, partner) -> test(villager, partner) || b.test(villager, partner);
        }

        @Override
        default Predicate negate() {
            return (villager, partner) -> !test(villager, partner);
        }

        default BiPredicate<VillagerLike<?>, Entity> asConstraint() {
            return (villager, player) -> villager instanceof CompassionateEntity<?> && (test((CompassionateEntity<?>)villager, player));
        }
    }
}

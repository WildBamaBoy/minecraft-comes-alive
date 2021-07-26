package mca.entity.ai;

import mca.Config;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.RelationshipType;
import mca.item.ItemsMCA;
import mca.item.SpecialCaseGift;
import mca.resources.API;
import mca.server.world.data.FamilyTree;
import mca.server.world.data.FamilyTreeEntry;
import mca.util.WorldUtils;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.block.SpongeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * I know you, you know me, we're all a big happy family.
 */
public class Relationship implements EntityRelationship {
    public static final Predicate IS_MARRIED = (villager, player) -> villager.getRelationships().isMarriedTo(player);
    public static final Predicate IS_RELATIVE = (villager, player) -> villager.getRelationships().getFamilyTree().isRelative(villager.getUuid(), player);
    public static final Predicate IS_FAMILY = IS_MARRIED.or(IS_RELATIVE);
    public static final Predicate IS_PARENT = (villager, player) -> villager.getRelationships().getFamilyTree().isParent(villager.getUuid(), player);

    private static final CDataParameter<Boolean> IS_PROCREATING = CParameter.create("isProcreating", false);
    private static final CDataParameter<String> SPOUSE_NAME = CParameter.create("spouseName", "");
    private static final CDataParameter<Optional<UUID>> SPOUSE_UUID = CParameter.create("spouseUUID", Optional.empty());
    private static final CEnumParameter<MarriageState> MARRIAGE_STATE = CParameter.create("marriageState", MarriageState.SINGLE);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder
                .addAll(IS_PROCREATING, SPOUSE_NAME, SPOUSE_UUID, MARRIAGE_STATE)
                .add(Pregnancy::createTrackedData);
    }

    private final VillagerEntityMCA entity;

    //gift desaturation queue
    final List<String> giftDesaturation = new LinkedList<>();

    private int procreateTick = -1;

    private final Pregnancy pregnancy;

    public Relationship(VillagerEntityMCA entity) {
        this.entity = entity;
        pregnancy = new Pregnancy(entity);
    }

    @Override
    public Gender getGender() {
        return entity.getGenetics().getGender();
    }

    public Pregnancy getPregnancy() {
        return pregnancy;
    }

    public boolean isProcreating() {
        return entity.getTrackedValue(IS_PROCREATING);
    }

    public void startProcreating() {
        procreateTick = 60;
        entity.setTrackedValue(IS_PROCREATING, true);
    }

    public Optional<Text> getSpouseName() {
        return isMarried() ? Optional.ofNullable(entity.getTrackedValue(SPOUSE_NAME)).map(LiteralText::new) : Optional.empty();
    }

    @Override
    public Optional<Entity> getSpouse() {
        return entity.getTrackedValue(SPOUSE_UUID).map(id -> ((ServerWorld) entity.world).getEntity(id));
    }

    @Override
    public FamilyTree getFamilyTree() {
        return FamilyTree.get((ServerWorld) entity.world);
    }

    @Override
    public FamilyTreeEntry getFamily() {
        return getFamilyTree().getOrCreate(entity);
    }

    @Override
    public Stream<Entity> getParents() {
        return getFamily().parents().map(((ServerWorld) entity.world)::getEntity).filter(Objects::nonNull);
    }

    @Override
    public Stream<Entity> getSiblings() {
        return getFamilyTree()
                .getSiblings(entity.getUuid())
                .stream()
                .map(id -> ((ServerWorld) entity.world).getEntity(id))
                .filter(Objects::nonNull)
                .filter(e -> !e.equals(entity)); // we exclude ourselves from the list of siblings
    }

    public void tick(int age) {
        if (age % 20 == 0) {
            pregnancy.tick();
        }

        if (!isProcreating()) {
            return;
        }

        Random random = entity.getRandom();
        if (procreateTick > 0) {
            procreateTick--;
            entity.getNavigation().stop();
            entity.world.sendEntityStatus(entity, Status.VILLAGER_HEARTS);
        } else {
            // TODO: Move this to the Pregnancy
            //make sure this villager is registered in the family tree
            getFamilyTree().getOrCreate(entity);
            getSpouse().ifPresent(spouse -> {
                ItemStack stack = (random.nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL).getDefaultStack();
                if (!(spouse instanceof PlayerEntity && ((PlayerEntity) spouse).giveItemStack(stack))) {
                    entity.getInventory().addStack(stack);
                }
            });

            entity.setTrackedValue(IS_PROCREATING, false);
        }
    }

    public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite) {
        // The death of a villager negatively modifies the mood of nearby strangers
        WorldUtils
                .getCloseEntities(entity.world, entity, 32, VillagerEntityMCA.class)
                .forEach(villager -> villager.getRelationships().onTragedy(cause, burialSite, RelationshipType.STRANGER));

        onTragedy(cause, burialSite, RelationshipType.SIBLING);
    }

    @Override
    public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type) {
        int moodAffect = 10;

        moodAffect /= type.getInverseProximity();
        moodAffect *= type.getProximityAmplifier();

        ((ServerWorld) entity.world).sendEntityStatus(entity, Status.MCA_VILLAGER_TRAGEDY);
        entity.getVillagerBrain().modifyMoodLevel(-moodAffect);

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
    public boolean isMarried() {
        return !entity.getTrackedValue(SPOUSE_UUID).orElse(Util.NIL_UUID).equals(Util.NIL_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return entity.getTrackedValue(SPOUSE_UUID).orElse(Util.NIL_UUID).equals(uuid);
    }

    public void marry(PlayerEntity player) {
        entity.setTrackedValue(SPOUSE_UUID, Optional.of(player.getUuid()));
        entity.setTrackedValue(SPOUSE_NAME, player.getName().getString());
        entity.setTrackedValue(MARRIAGE_STATE, MarriageState.MARRIED_TO_PLAYER);
    }

    public void marry(VillagerEntityMCA spouse) {
        entity.setTrackedValue(SPOUSE_UUID, Optional.of(spouse.getUuid()));
        entity.setTrackedValue(SPOUSE_NAME, spouse.getName().getString());
        entity.setTrackedValue(MARRIAGE_STATE, MarriageState.MARRIED_TO_VILLAGER);
    }

    @Override
    public void endMarriage(MarriageState newState) {
        entity.setTrackedValue(SPOUSE_UUID, Optional.empty());
        entity.setTrackedValue(SPOUSE_NAME, "");
        entity.setTrackedValue(MARRIAGE_STATE, newState);
    }

    public void giveGift(ServerPlayerEntity player, Memories memory) {
        ItemStack stack = player.getMainHandStack();

        if (!stack.isEmpty()) {
            int giftValue = API.getGiftPool().getWorth(stack);
            if (!handleSpecialCaseGift(player, stack)) {
                if (stack.getItem() == Items.GOLDEN_APPLE) {
                    //TODO special
                    entity.setInfected(false);
                } else if (stack.getItem() instanceof DyeItem) {
                    //TODO special
                    DyeItem dye = (DyeItem) stack.getItem();
                    entity.setHairDye(dye.getColor());
                } else if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof SpongeBlock) {
                    //TODO special, also feels super hacky, probably a better way to check for blocks
                    entity.setHairDye();
                } else {
                    // TODO: Don't use translation keys. Use identifiers.
                    String id = stack.getTranslationKey();
                    long occurrences = giftDesaturation.stream().filter(id::equals).count();

                    //check if desaturation fail happen
                    if (entity.getRandom().nextInt(100) < occurrences * Config.getInstance().giftDesaturationPenalty) {
                        giftValue = -giftValue / 2;
                        entity.sendChatMessage(player, API.getGiftPool().getResponseForSaturatedGift(stack));
                    } else {
                        entity.sendChatMessage(player, API.getGiftPool().getResponse(stack));
                    }

                    //modify mood and hearts
                    entity.getVillagerBrain().modifyMoodLevel(giftValue / 2 + 2 * MathHelper.sign(giftValue));
                    memory.modHearts(giftValue);
                }
            }

            //add to desaturation queue
            giftDesaturation.add(stack.getTranslationKey());
            while (giftDesaturation.size() > Config.getInstance().giftDesaturationQueueLength) {
                giftDesaturation.remove(0);
            }

            //particles
            if (giftValue > 0) {
                player.getMainHandStack().decrement(1);
                entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_POS_INTERACTION);
            } else {
                entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_NEG_INTERACTION);
            }
        }
    }

    private boolean handleSpecialCaseGift(ServerPlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift) item).handle(player, entity)) {
                player.getMainHandStack().decrement(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !entity.isBaby()) {
                if (pregnancy.tryStartGestation()) {
                    ((ServerWorld) player.world).sendEntityStatus(entity, Status.VILLAGER_HEARTS);
                    entity.sendChatMessage(player, "gift.cake.success");
                } else {
                    entity.sendChatMessage(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && entity.isBaby()) {
            // increase age by 5 minutes
            entity.growUp(1200 * 5);
            return true;
        }

        return false;
    }

    public void readFromNbt(NbtCompound nbt) {
        //load gift desaturation queue
        NbtList res = nbt.getList("giftDesaturation", 8);
        for (int i = 0; i < res.size(); i++) {
            String c = res.getString(i);
            giftDesaturation.add(c);
        }
    }

    public void writeToNbt(NbtCompound nbt) {
        //save gift desaturation queue
        NbtList giftDesaturationQueue = new NbtList();
        for (int i = 0; i < giftDesaturation.size(); i++) {
            giftDesaturationQueue.addElement(i, NbtString.of(giftDesaturation.get(i)));
        }
        nbt.put("giftDesaturation", giftDesaturationQueue);
    }

    public interface Predicate extends BiPredicate<VillagerEntityMCA, Entity> {

        boolean test(VillagerEntityMCA villager, UUID partner);

        @Override
        default boolean test(VillagerEntityMCA villager, Entity partner) {
            return partner != null && test(villager, partner.getUuid());
        }

        default Predicate or(Predicate b) {
            return (villager, partner) -> test(villager, partner) || b.test(villager, partner);
        }

        @Override
        default Predicate negate() {
            return (villager, partner) -> !test(villager, partner);
        }
    }
}

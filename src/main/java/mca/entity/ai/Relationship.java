package mca.entity.ai;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import mca.MCA;
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
import mca.util.network.datasync.CBooleanParameter;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CStringParameter;
import mca.util.network.datasync.CUUIDParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

/**
 * I know you, you know me, we're all a big happy family.
 */
public class Relationship implements EntityRelationship {
    public static final Predicate IS_MARRIED = (villager, player) -> villager.getRelationships().isMarriedTo(player);
    public static final Predicate IS_RELATIVE = (villager, player) -> villager.getRelationships().getFamilyTree().isRelative(villager.getUuid(), player);
    public static final Predicate IS_FAMILY = IS_MARRIED.or(IS_RELATIVE);
    public static final Predicate IS_PARENT = (villager, player) -> villager.getRelationships().getFamilyTree().isParent(villager.getUuid(), player);

    private final VillagerEntityMCA entity;

    //gift desaturation queue
    final List<String> giftDesaturation = new LinkedList<>();

    private final CBooleanParameter isProcreating;
    private int procreateTick = -1;

    private final CStringParameter spouseName;
    private final CUUIDParameter spouseUUID;

    private final CEnumParameter<MarriageState> marriageState;

    private final Pregnancy pregnancy;

    public Relationship(VillagerEntityMCA entity, CDataManager data) {
        this.entity = entity;
        isProcreating = data.newBoolean("isProcreating");
        spouseName = data.newString("spouseName");
        spouseUUID = data.newUUID("spouseUUID");
        marriageState = data.newEnum("marriageState", MarriageState.SINGLE);
        pregnancy = new Pregnancy(entity, data);
    }

    @Override
    public Gender getGender() {
        return entity.getGenetics().getGender();
    }

    public Pregnancy getPregnancy() {
        return pregnancy;
    }

    public boolean isProcreating() {
        return isProcreating.get();
    }

    public void startProcreating() {
        procreateTick = 60;
        isProcreating.set(true);
    }

    public Optional<Text> getSpouseName() {
        return isMarried() ? Optional.ofNullable(spouseName.get()).map(LiteralText::new) : Optional.empty();
    }

    @Override
    public Optional<Entity> getSpouse() {
        return spouseUUID.get().map(id -> ((ServerWorld) entity.world).getEntity(id));
    }

    @Override
    public FamilyTree getFamilyTree() {
        return FamilyTree.get((ServerWorld)entity.world);
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
                if (!(spouse instanceof PlayerEntity && ((PlayerEntity)spouse).giveItemStack(stack))) {
                    entity.getInventory().addStack(stack);
                }
            });

            isProcreating.set(false);
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

        ((ServerWorld)entity.world).sendEntityStatus(entity, Status.MCA_VILLAGER_TRAGEDY);
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
        return marriageState.get();
    }

    @Override
    public boolean isMarried() {
        return !spouseUUID.get().orElse(Util.NIL_UUID).equals(Util.NIL_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return spouseUUID.get().orElse(Util.NIL_UUID).equals(uuid);
    }

    public void marry(PlayerEntity player) {
        spouseUUID.set(player.getUuid());
        spouseName.set(player.getName().asString());
        marriageState.set(MarriageState.MARRIED_TO_PLAYER);
    }

    public void marry(VillagerEntityMCA spouse) {
        spouseUUID.set(spouse.getUuid());
        spouseName.set(spouse.villagerName.get());
        marriageState.set(MarriageState.MARRIED_TO_VILLAGER);
    }

    @Override
    public void endMarriage(MarriageState newState) {
        spouseUUID.set(Util.NIL_UUID);
        spouseName.set("");
        marriageState.set(newState);
    }

    public void giveGift(ServerPlayerEntity player, Memories memory) {
        ItemStack stack = player.getMainHandStack();

        if (!stack.isEmpty()) {
            int giftValue = API.getGiftPool().getWorth(stack);
            if (!handleSpecialCaseGift(player, stack)) {
                if (stack.getItem() == Items.GOLDEN_APPLE) {
                    //TODO special
                    entity.setInfected(false);
                } else {
                    // TODO: Don't use translation keys. Use identifiers.
                    String id = stack.getTranslationKey();
                    long occurrences = giftDesaturation.stream().filter(id::equals).count();

                    //check if desaturation fail happen
                    if (entity.getRandom().nextInt(100) < occurrences * MCA.getConfig().giftDesaturationPenalty) {
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
            while (giftDesaturation.size() > MCA.getConfig().giftDesaturationQueueLength) {
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
                    ((ServerWorld)player.world).sendEntityStatus(entity, Status.VILLAGER_HEARTS);
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

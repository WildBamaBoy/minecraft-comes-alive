package mca.entity.ai;

import mca.Config;
import mca.TagsMCA;
import mca.advancement.criterion.CriterionMCA;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.entity.interaction.gifts.GiftType;
import mca.entity.interaction.gifts.Response;
import mca.item.SpecialCaseGift;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

import java.util.*;

/**
 * I know you, you know me, we're all a big happy family.
 */
public class BreedableRelationship extends Relationship<VillagerEntityMCA> {

    private static final CDataParameter<Boolean> IS_PROCREATING = CParameter.create("isProcreating", false);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return Relationship.createTrackedData(builder)
                .addAll(IS_PROCREATING)
                .add(Pregnancy::createTrackedData);
    }

    private int procreateTick = -1;

    private final Pregnancy pregnancy;

    public BreedableRelationship(VillagerEntityMCA entity) {
        super(entity);
        pregnancy = new Pregnancy(entity);
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
                boolean areTwins = random.nextInt(100) < Config.getInstance().chanceToHaveTwins;
                int count = areTwins ? 2 : 1;

                // advancement
                if (spouse instanceof ServerPlayerEntity) {
                    CriterionMCA.BABY_CRITERION.trigger((ServerPlayerEntity) spouse, count);
                }

                for (int i = 0; i < count; i++) {
                    ItemStack stack = TagsMCA.Items.BABIES.getRandom(random).getDefaultStack();
                    if (!(spouse instanceof PlayerEntity && ((PlayerEntity) spouse).giveItemStack(stack))) {
                        entity.getInventory().addStack(stack);
                    }
                }
            });

            entity.setTrackedValue(IS_PROCREATING, false);
        }
    }

    public void giveGift(ServerPlayerEntity player, Memories memory) {
        ItemStack stack = player.getMainHandStack();

        if (!stack.isEmpty() && !handleSpecialCaseGift(player, stack)) {
            Optional<GiftType> gift = GiftType.firstMatching(stack);

            if (gift.isPresent()) {
                acceptGift(stack, gift.get(), player, memory);
            } else {
                if (stack.getItem() == Items.GOLDEN_APPLE) {
                    entity.setInfected(false);
                    entity.eatFood(entity.world, stack);
                } else if (stack.getItem() instanceof DyeItem) {
                    entity.setHairDye(((DyeItem) stack.getItem()).getColor());
                    stack.decrement(1);
                } else if (stack.getItem() == Items.SPONGE) {
                    entity.clearHairDye();
                    stack.decrement(1);
                } else {
                    rejectGift(player, "gift.fail");
                }
            }
        }
    }

    private void acceptGift(ItemStack stack, GiftType gift, PlayerEntity player, Memories memory) {

        float satisfaction = gift.getSatisfactionFor(entity, stack);
        Response response = gift.getResponse(satisfaction);

        if (!entity.getInventory().canInsert(stack)) {
            rejectGift(player, "villager.inventory.full");
            return;
        }

        if (response == Response.FAIL) {
            rejectGift(player, gift.getDialogueFor(response));
            return;
        }

        long occurrences = getGiftSaturation().get(stack);

        //check if desaturation fail happen
        if (entity.getRandom().nextInt(100) < occurrences * Config.getInstance().giftDesaturationPenalty) {
            satisfaction = -satisfaction / 2;
            entity.sendChatMessage(player, "gift.saturated");
        } else {
            entity.sendChatMessage(player, gift.getDialogueFor(response));
            if (response == Response.BEST) {
                entity.playSurprisedSound();
            }
        }

        //modify mood and hearts
        entity.getVillagerBrain().modifyMoodLevel((int)(satisfaction / 2 + 2 * MathHelper.sign(satisfaction)));
        memory.modHearts((int)satisfaction);

        getGiftSaturation().add(stack, 1);
        entity.getInventory().addStack(player.getMainHandStack().split(1));
        entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_POS_INTERACTION);
    }

    private void rejectGift(PlayerEntity player, String dialogue) {
        entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_NEG_INTERACTION);
        entity.sendChatMessage(player, dialogue);
    }

    private boolean handleSpecialCaseGift(ServerPlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift) item).handle(player, entity)) {
                player.getMainHandStack().decrement(1);
                return true;
            }
        }

        if (item == Items.CAKE && isMarriedTo(player.getUuid()) && !entity.isBaby()) {
            if (pregnancy.tryStartGestation()) {
                ((ServerWorld) player.world).sendEntityStatus(entity, Status.VILLAGER_HEARTS);
                entity.sendChatMessage(player, "gift.cake.success");
            } else {
                entity.sendChatMessage(player, "gift.cake.fail");
            }

            return true;
        }

        if (item == Items.GOLDEN_APPLE && entity.isBaby()) {
            // increase age by 5 minutes
            entity.growUp(1200 * 5);
            return true;
        }

        return false;
    }
}

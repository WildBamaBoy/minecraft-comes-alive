package mca.entity.ai;

import mca.Config;
import mca.cobalt.network.NetworkHandler;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.entity.interaction.gifts.GiftType;
import mca.entity.interaction.gifts.Response;
import mca.item.SpecialCaseGift;
import mca.network.client.AnalysisResults;
import mca.resources.data.IntAnalysis;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
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

        if (procreateTick > 0) {
            procreateTick--;
            entity.getNavigation().stop();
            entity.world.sendEntityStatus(entity, Status.VILLAGER_HEARTS);
        } else {
            getFamilyTree().getOrCreate(entity);
            getSpouse().ifPresent(spouse -> {
                pregnancy.procreate(spouse);

                entity.setTrackedValue(IS_PROCREATING, false);
            });
        }
    }

    public void giveGift(ServerPlayerEntity player, Memories memory) {
        ItemStack stack = player.getMainHandStack();

        if (!stack.isEmpty() && !handleSpecialCaseGift(player, stack)) {
            if (stack.getItem() == Items.GOLDEN_APPLE) {
                entity.setInfected(false);
                entity.eatFood(entity.world, stack);
                stack.decrement(1);
            } else if (stack.getItem() instanceof DyeItem) {
                entity.setHairDye(((DyeItem)stack.getItem()).getColor());
                stack.decrement(1);
            } else if (stack.getItem() == Items.WET_SPONGE) {
                entity.clearHairDye();
                stack.decrement(1);
            } else if (stack.getItem() == Items.NAME_TAG) {
                if (stack.hasCustomName()) {
                    entity.setCustomSkin(stack.getName().asString());
                } else {
                    entity.setCustomSkin("");
                }
                stack.decrement(1);
            } else {
                Optional<GiftType> gift = GiftType.bestMatching(entity, stack);

                // gift is unknown
                if (gift.isPresent()) {
                    acceptGift(stack,gift.get(), player, memory);
                } else {
                    rejectGift(player, "gift.fail");
                }
            }
        }
    }

    private void acceptGift(ItemStack stack, GiftType gift, PlayerEntity player, Memories memory) {
        // inventory full
        if (!entity.getInventory().canInsert(stack)) {
            rejectGift(player, "villager.inventory.full");
            return;
        }

        IntAnalysis analysis = gift.getSatisfactionFor(entity, stack);
        int satisfaction = analysis.getTotal();
        Response response = gift.getResponse(satisfaction);

        // desaturation
        int occurrences = getGiftSaturation().get(stack);
        int penalty = (int)(occurrences * Config.getInstance().giftDesaturationFactor * Math.pow(Math.max(satisfaction, 0.0), Config.getInstance().giftDesaturationExponent));
        if (penalty != 0) {
            analysis.add("desaturation", -penalty);
        }
        int desaturatedSatisfaction = analysis.getTotal();
        Response desaturatedResponse = gift.getResponse(desaturatedSatisfaction);

        // adjust reward
        desaturatedSatisfaction *= Config.getInstance().giftSatisfactionFactor;

        NetworkHandler.sendToPlayer(new AnalysisResults(analysis), (ServerPlayerEntity)player);

        if (response == Response.FAIL) {
            rejectGift(player, gift.getDialogueFor(response));
        } else if (desaturatedResponse == Response.FAIL) {
            rejectGift(player, "gift.saturated");
        } else {
            entity.sendChatMessage(player, gift.getDialogueFor(response));
            if (response == Response.BEST) {
                entity.playSurprisedSound();
            }

            //take the gift
            getGiftSaturation().add(stack);
            entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_POS_INTERACTION);
            entity.getInventory().addStack(stack.split(1));
        }

        //modify mood and hearts
        entity.getVillagerBrain().modifyMoodValue(desaturatedSatisfaction / 2 + Config.getInstance().baseGiftMoodEffect * MathHelper.sign(desaturatedSatisfaction));
        memory.modHearts(desaturatedSatisfaction);
    }

    private void rejectGift(PlayerEntity player, String dialogue) {
        entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_NEG_INTERACTION);
        entity.sendChatMessage(player, dialogue);
    }

    private boolean handleSpecialCaseGift(ServerPlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift)item).handle(player, entity)) {
                player.getMainHandStack().decrement(1);
            }
            return true;
        }

        if (item == Items.CAKE && !entity.isBaby()) {
            if (pregnancy.tryStartGestation()) {
                player.world.sendEntityStatus(entity, Status.VILLAGER_HEARTS);
                player.getMainHandStack().decrement(1);
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

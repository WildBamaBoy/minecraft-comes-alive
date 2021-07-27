package mca.entity.interaction;

import mca.advancement.criterion.CriterionMCA;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Chore;
import mca.entity.ai.Memories;
import mca.entity.ai.MoveState;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.Personality;
import mca.item.ItemsMCA;
import mca.server.world.data.PlayerSaveData;
import mca.util.compat.OptionalCompat;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

import java.util.Comparator;

public class VillagerCommandHandler extends EntityCommandHandler<VillagerEntityMCA> {

    public VillagerCommandHandler(VillagerEntityMCA entity) {
        super(entity);
    }

    /**
     * Called on the server to respond to button events.
     */
    @Override
    public boolean handle(ServerPlayerEntity player, String command) {
        Memories memory = entity.getVillagerBrain().getMemoriesForPlayer(player);

        if (Interaction.byCommand(command).filter(interaction -> {
            handleInteraction(player, memory, interaction);
            return true;
        }).isPresent()) {
            return true;
        }

        if (MoveState.byCommand(command).filter(state -> {
            entity.getVillagerBrain().setMoveState(state, player);
            return true;
        }).isPresent()) {
            return true;
        }

        if (Chore.byCommand(command).filter(chore -> {
            entity.getVillagerBrain().assignJob(chore, player);
            return true;
        }).isPresent()) {
            return true;
        }

        switch (command) {
            case "pickup":
                if (entity.hasVehicle()) {
                    entity.stopRiding();
                } else {
                    entity.startRiding(player, true);
                }

                player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));

                return true;
            case "ridehorse":
                if (entity.hasVehicle()) {
                    entity.stopRiding();
                } else {
                    OptionalCompat.ifPresentOrElse(entity.world.getOtherEntities(player, player.getBoundingBox()
                            .expand(10), e -> e instanceof Saddleable && ((Saddleable) e).isSaddled())
                            .stream()
                            .filter(horse -> !horse.hasPassengers())
                            .min(Comparator.comparingDouble(a -> a.squaredDistanceTo(entity))), horse -> {
                        entity.startRiding(horse, false);
                        entity.sendChatMessage(player, "interaction.ridehorse.success");
                    }, () -> entity.sendChatMessage(player, "interaction.ridehorse.fail.notnearby"));
                }
                return true;
            case "sethome":
                entity.getResidency().setHome(player);
                return true;
            case "gohome":
                entity.getResidency().goHome(player);
                stopInteracting();
                return false;
            case "setworkplace":
                entity.getResidency().setWorkplace(player);
                return true;
            case "sethangout":
                entity.getResidency().setHangout(player);
                return true;
            case "trade":
                prepareOffersFor(player);
                return false;
            case "inventory":
                player.openHandledScreen(entity);
                return false;
            case "gift":
                entity.getRelationships().giveGift(player, memory);
                return true;
            case "procreate":
                if (PlayerSaveData.get((ServerWorld) entity.world, player.getUuid()).isBabyPresent()) {
                    entity.sendChatMessage(player, "interaction.procreate.fail.hasbaby");
                } else if (memory.getHearts() < 100) {
                    entity.sendChatMessage(player, "interaction.procreate.fail.lowhearts");
                } else {
                    entity.getRelationships().startProcreating();
                }
                return true;
            case "divorcePapers":
                player.inventory.insertStack(new ItemStack(ItemsMCA.DIVORCE_PAPERS));
                entity.sendChatMessage(player, "cleric.divorcePapers");
                return true;
            case "divorceConfirm":
                ItemStack papers = ItemsMCA.DIVORCE_PAPERS.getDefaultStack();
                Memories memories = entity.getVillagerBrain().getMemoriesForPlayer(player);
                if (player.inventory.contains(papers)) {
                    entity.sendChatMessage(player, "divorcePaper");
                    player.inventory.removeOne(papers);
                    memories.modHearts(-20);
                } else {
                    entity.sendChatMessage(player, "divorce");
                    memories.modHearts(-200);
                }
                entity.getVillagerBrain().modifyMoodLevel(-5);
                entity.getRelationships().endMarriage(MarriageState.SINGLE);

                PlayerSaveData playerData = PlayerSaveData.get((ServerWorld) player.world, player.getUuid());
                playerData.endMarriage(MarriageState.SINGLE);

                return true;
            case "execute":
                entity.setProfession(ProfessionsMCA.OUTLAW);
                return true;
            case "pardon":
                entity.setProfession(VillagerProfession.NONE);
                return true;
            case "infected":
                entity.setInfected(!entity.isInfected());
                return false;
            case "profession":
                entity.setProfession(ProfessionsMCA.randomProfession());
                return false;
            case "stopworking":
                entity.getVillagerBrain().abandonJob();
                return true;
        }

        return super.handle(player, command);
    }

    private void prepareOffersFor(PlayerEntity player) {
        int i = entity.getReputation(player);
        if (i != 0) {
            for (TradeOffer merchantoffer : entity.getOffers()) {
                merchantoffer.increaseSpecialPrice(-MathHelper.floor(i * merchantoffer.getPriceMultiplier()));
            }
        }

        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            StatusEffectInstance effectinstance = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
            if (effectinstance != null) {
                int k = effectinstance.getAmplifier();

                for (TradeOffer merchantOffer : entity.getOffers()) {
                    double d0 = 0.3D + 0.0625D * k;
                    int j = (int) Math.floor(d0 * merchantOffer.getOriginalFirstBuyItem().getCount());
                    merchantOffer.increaseSpecialPrice(-Math.max(j, 1));
                }
            }
        }

        entity.setCurrentCustomer(player);
        entity.sendOffers(player, entity.getDisplayName(), entity.getVillagerData().getLevel());
    }

    private void handleInteraction(ServerPlayerEntity player, Memories memory, Interaction interaction) {
        //success chance and hearts
        float successChance = 0.85F;
        int heartsBoost = 5;
        if (interaction != null) {
            heartsBoost = interaction.getHearts(entity.getVillagerBrain());
            successChance = interaction.getSuccessChance(entity.getVillagerBrain(), memory) / 100.0f;
        }

        boolean succeeded = entity.getRandom().nextFloat() < successChance;

        //spawn particles
        if (succeeded) {
            entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_POS_INTERACTION);
        } else {
            entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_NEG_INTERACTION);

            //sensitive people doubles the loss
            if (entity.getVillagerBrain().getPersonality() == Personality.SENSITIVE) {
                heartsBoost *= 2;
            }
        }

        memory.modInteractionFatigue(1);
        heartsBoost = succeeded ? heartsBoost : -heartsBoost;
        memory.modHearts(heartsBoost);
        CriterionMCA.HEARTS_CRITERION.trigger(player, memory.getHearts(), heartsBoost, "interaction");
        entity.getVillagerBrain().modifyMoodLevel(succeeded ? heartsBoost : -heartsBoost);
        entity.sendChatMessage(player, String.format("%s.%s", interaction.name().toLowerCase(), succeeded ? "success" : "fail"));
    }
}

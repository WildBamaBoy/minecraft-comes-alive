package mca.entity.ai;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mca.MCA;
import mca.client.gui.GuiInteract;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.Personality;
import mca.item.ItemsMCA;
import mca.resources.API;
import mca.resources.data.Button;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public class Interactions {
    private final VillagerEntityMCA entity;

    @Nullable
    private PlayerEntity interactingPlayer;

    public Interactions(VillagerEntityMCA entity) {
        this.entity = entity;
    }

    public Optional<PlayerEntity> getInteractingPlayer() {
        return Optional.ofNullable(interactingPlayer).filter(player -> player.currentScreenHandler != null);
    }

    public void stopInteracting() {
        if (!entity.world.isClient) {
            if (interactingPlayer instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)interactingPlayer).closeHandledScreen();
            }
        }
        interactingPlayer = null;
    }

    public ActionResult interactAt(PlayerEntity player, Vec3d pos, @NotNull Hand hand) {
        if (entity.world.isClient) {
            openScreen(player);
            return ActionResult.SUCCESS;
        } else {
            interactingPlayer = player;
            return ActionResult.PASS;
        }
    }

    private void openScreen(PlayerEntity player) {
        MinecraftClient.getInstance().openScreen(new GuiInteract(entity, player));
    }

    /**
     * Called on the server to respond to button events.
     */
    public void handle(ServerPlayerEntity player, String guiKey, String buttonId) {
        Memories memory = entity.getVillagerBrain().getMemoriesForPlayer(player);
        Optional<Button> button = API.getScreenComponents().getButton(guiKey, buttonId);
        if (!button.isPresent()) {
            MCA.logger.info("Button not found for key and ID: " + guiKey + ", " + buttonId);
        } else if (button.get().isInteraction()) {
            handleInteraction(player, memory, button.get());
        }

        switch (buttonId) {
            case "gui.button.move":
            case "gui.button.stay":
            case "gui.button.follow":
                MoveState.byAction(buttonId).ifPresent(state -> {
                    entity.getVillagerBrain().setMoveState(state, player);
                });
                stopInteracting();
                break;
            case "gui.button.pickup":
                if (entity.hasVehicle()) {
                    entity.stopRiding();
                } else {
                    entity.startRiding(player, true);
                }

                if (player instanceof ServerPlayerEntity) {
                    player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
                }

                stopInteracting();
                break;
            case "gui.button.ridehorse":
                if (entity.hasVehicle()) {
                    entity.stopRiding();
                } else {
                    entity.world.getOtherEntities(player, player.getBoundingBox().expand(10), e -> e instanceof Saddleable && ((Saddleable)e).isSaddled())
                        .stream()
                        .filter(horse -> !horse.hasPassengers())
                        .sorted((a, b) -> Double.compare(a.squaredDistanceTo(entity), b.squaredDistanceTo(entity)))
                        .findFirst().ifPresentOrElse(horse -> {
                            entity.startRiding(horse, false);
                            entity.sendChatMessage(player, "command.ride.success");
                        }, () -> entity.sendChatMessage(player, "command.ride.fail.no_horse"));
                }
                stopInteracting();
                break;
            case "gui.button.sethome":
                entity.getResidency().setHome(player);
                stopInteracting();
                break;
            case "gui.button.gohome":
                entity.getResidency().goHome(player);
                stopInteracting();
                break;
            case "gui.button.setworkplace":
                entity.getResidency().setWorkplace(player);
                stopInteracting();
                break;
            case "gui.button.sethangout":
                entity.getResidency().setHangout(player);
                stopInteracting();
                break;
            case "gui.button.trade":
                prepareOffersFor(player);
                break;
            case "gui.button.inventory":
                player.openHandledScreen(entity);
                break;
            case "gui.button.gift":
                entity.getRelationships().giveGift(player, memory);
                stopInteracting();
                break;
            case "gui.button.procreate":
                if (PlayerSaveData.get((ServerWorld)entity.world, player.getUuid()).isBabyPresent()) {
                    entity.sendChatMessage(player, "interaction.procreate.fail.hasbaby");
                } else if (memory.getHearts() < 100) {
                    entity.sendChatMessage(player, "interaction.procreate.fail.lowhearts");
                } else {
                    entity.getRelationships().startProcreating();
                }
                stopInteracting();
                break;
            case "gui.button.divorcePapers":
                player.getInventory().insertStack(new ItemStack(ItemsMCA.DIVORCE_PAPERS));
                entity.sendChatMessage(player, "cleric.divorcePapers");
                stopInteracting();
                break;
            case "gui.button.divorceConfirm":
                //this lambda is meh

                int divorcePaper = player.getInventory().indexOf(ItemsMCA.DIVORCE_PAPERS.getDefaultStack());
                Memories memories = entity.getVillagerBrain().getMemoriesForPlayer(player);
                if (divorcePaper >= 0) {
                    entity.sendChatMessage(player, "divorcePaper");
                    player.getInventory().getStack(divorcePaper).decrement(1);
                    memories.modHearts(-20);
                } else {
                    entity.sendChatMessage(player, "divorce");
                    memories.modHearts(-200);
                }
                entity.getVillagerBrain().modifyMoodLevel(-5);
                entity.getRelationships().endMarriage(MarriageState.SINGLE);

                PlayerSaveData playerData = PlayerSaveData.get((ServerWorld)player.world, player.getUuid());
                playerData.endMarriage(MarriageState.SINGLE);

                stopInteracting();
                break;
            case "gui.button.execute":
                entity.setProfession(ProfessionsMCA.OUTLAW);
                stopInteracting();
                break;
            case "gui.button.pardon":
                entity.setProfession(VillagerProfession.NONE);
                stopInteracting();
                break;
            case "gui.button.infected":
                entity.setInfected(!entity.isInfected());
                break;
            case "gui.button.clothing.randClothing":
                entity.clothes.set(API.getClothingPool().pickOne(entity));
                break;
            case "gui.button.clothing.prevClothing":
                entity.clothes.set(API.getClothingPool().pickNext(entity, entity.clothes.get(), -1));
                break;
            case "gui.button.clothing.nextClothing":
                entity.clothes.set(API.getClothingPool().pickNext(entity, entity.clothes.get(), 1));
                break;
            case "gui.button.clothing.randHair":
                entity.setHair(API.getHairPool().pickOne(entity));
                break;
            case "gui.button.clothing.prevHair":
                entity.setHair(API.getHairPool().pickNext(entity, entity.getHair(), -1));
                break;
            case "gui.button.clothing.nextHair":
                entity.setHair(API.getHairPool().pickNext(entity, entity.getHair(), 1));
                break;
            case "gui.button.profession":
                entity.setProfession(ProfessionsMCA.randomProfession());
                break;
            case "gui.button.prospecting":
            case "gui.button.hunting":
            case "gui.button.fishing":
            case "gui.button.chopping":
            case "gui.button.harvesting":
                Chore.byAction(buttonId).ifPresent(chore -> {
                    entity.getVillagerBrain().assignJob(chore, player);
                });
                stopInteracting();
                break;
            case "gui.button.stopworking":
                entity.getVillagerBrain().abandonJob();
                stopInteracting();
                break;
        }
    }

    void prepareOffersFor(PlayerEntity player) {
        entity.sendOffers(player, entity.getDisplayName(), entity.getVillagerData().getLevel());


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
    }

    private void handleInteraction(PlayerEntity player, Memories memory, Button button) {
        //interaction
        String interactionName = button.identifier().replace("gui.button.", "");
        Interaction interaction = Interaction.fromName(interactionName);

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
            entity.world.sendEntityStatus(entity, (byte) 16);
        } else {
            entity.world.sendEntityStatus(entity, (byte) 15);

            //sensitive people doubles the loss
            if (entity.getVillagerBrain().getPersonality() == Personality.SENSITIVE) {
                heartsBoost *= 2;
            }
        }

        memory.modInteractionFatigue(1);
        memory.modHearts(succeeded ? heartsBoost : -heartsBoost);
        entity.getVillagerBrain().modifyMoodLevel(succeeded ? heartsBoost : -heartsBoost);

        entity.sendChatMessage(player, String.format("%s.%s", interactionName, succeeded ? "success" : "fail"));
        stopInteracting();
    }

}

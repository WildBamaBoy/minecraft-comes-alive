package mca.entity;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mca.api.API;
import mca.api.types.Button;
import mca.client.gui.GuiInteract;
import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.Chore;
import mca.enums.Interaction;
import mca.enums.MoveState;
import mca.enums.Personality;
import mca.items.SpecialCaseGift;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;

public class Interactions {
    private final VillagerEntityMCA entity;

    @Nullable
    private PlayerEntity interactingPlayer;

    Interactions(VillagerEntityMCA entity) {
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
    public void handle(PlayerEntity player, String guiKey, String buttonId) {
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
            case "gui.button.ridehorse":
//                toggleMount(player);
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
                giveGift(player, memory);
                stopInteracting();
                break;
            case "gui.button.procreate":
                if (PlayerSaveData.get(entity.world, player.getUuid()).isBabyPresent()) {
                    entity.sendChatMessage(player, "interaction.procreate.fail.hasbaby");
                } else if (memory.getHearts() < 100) {
                    entity.sendChatMessage(player, "interaction.procreate.fail.lowhearts");
                } else {
                    entity.procreateTick = 60;
                    entity.isProcreating.set(true);
                }
                stopInteracting();
                break;
            case "gui.button.infected":
                entity.isInfected.set(!entity.isInfected.get());
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

    private void giveGift(PlayerEntity player, Memories memory) {
        ItemStack stack = player.getMainHandStack();

        if (!stack.isEmpty()) {
            int giftValue = API.getGiftPool().getWorth(stack);
            if (!handleSpecialCaseGift(player, stack)) {
                if (stack.getItem() == Items.GOLDEN_APPLE) {
                    //TODO special
                    entity.isInfected.set(false);
                } else {
                    // TODO: Don't use translation keys. Use identifiers.
                    String id = stack.getTranslationKey();
                    long occurrences = entity.giftDesaturation.stream().filter(id::equals).count();

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
            entity.giftDesaturation.add(stack.getTranslationKey());
            while (entity.giftDesaturation.size() > MCA.getConfig().giftDesaturationQueueLength) {
                entity.giftDesaturation.remove(0);
            }

            //particles
            if (giftValue > 0) {
                player.getMainHandStack().decrement(1);
                entity.world.sendEntityStatus(entity, (byte) 16);
            } else {
                entity.world.sendEntityStatus(entity, (byte) 15);
            }
        }
    }

    private boolean handleSpecialCaseGift(PlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift) item).handle(player, entity)) {
                player.getMainHandStack().decrement(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (entity.isMarried() && !entity.isBaby()) {
                if (entity.getPregnancy().tryStartGestation()) {
                    entity.produceParticles(ParticleTypes.HEART);
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
}

package mca.server;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import mca.core.minecraft.ItemsMCA;
import mca.entity.data.PlayerSaveData;
import mca.enums.MarriageState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import java.util.*;

public class ServerInteractionManager {

    private static final ServerInteractionManager INSTANCE = new ServerInteractionManager();

    /**
     * Maps a player's UUID to a list of UUIDs that have proposed to them with /mca propose
     */
    private final Map<UUID, List<UUID>> proposals = new HashMap<>();

    /**
     * List of UUIDs that initiated procreation mapped to the time the request expires.
     */
    private final Object2LongArrayMap<UUID> procreateMap = new Object2LongArrayMap<>();


    private ServerInteractionManager() { }

    public static ServerInteractionManager getInstance() {
        return INSTANCE;
    }

    public void tick() {
        List<UUID> removals = new ArrayList<>();
        procreateMap.keySet().stream()
                .filter((k) -> procreateMap.getLong(k) < System.currentTimeMillis())
                .forEach(removals::add);
        removals.forEach(procreateMap::removeLong);
    }

    /**
     * Returns true if receiver has a proposal from sender.
     *
     * @param sender   Command sender
     * @param receiver Player whose name was entered by the sender
     * @return boolean
     */
    private boolean hasProposalFrom(PlayerEntity sender, PlayerEntity receiver) {
        return getProposalsFor(receiver).contains(sender.getUuid());
    }

    /**
     * Returns all proposals for the provided player
     *
     * @param player Player whose proposals should be returned.
     * @return List<UUID>
     */
    private List<UUID> getProposalsFor(PlayerEntity player) {
        return proposals.getOrDefault(player.getUuid(), new ArrayList<>());
    }

    /**
     * Removes the provided proposer from the target's list of proposals.
     *
     * @param target   Target player who's proposal list will be modified.
     * @param proposer The proposer to the target player.
     */
    private void removeProposalFor(PlayerEntity target, PlayerEntity proposer) {
        List<UUID> list = getProposalsFor(target);
        list.remove(proposer.getUuid());
        proposals.put(target.getUuid(), list);
    }

    /**
     * Lists all proposals for the given player.
     *
     * @param sender Player whose active proposals will be listed.
     */
    public void listProposals(PlayerEntity sender) {
        List<UUID> proposals = getProposalsFor(sender);

        if (proposals.size() == 0) {
            infoMessage(sender, "You have no active proposals.");
        } else {
            infoMessage(sender, "You have active proposals from: ");
        }

        // Send the name of all online players to the command sender.
        proposals.forEach((uuid -> {
            PlayerEntity player = sender.getEntityWorld().getPlayerByUuid(uuid);
            if (player != null) {
                infoMessage(sender, "- " + player.getEntityName());
            }
        }));
    }

    /**
     * Sends a proposal from the sender to the receiver.
     *
     * @param sender   The player sending the proposal.
     * @param receiver The player being proposed to.
     */
    public void sendProposal(PlayerEntity sender, PlayerEntity receiver) {
        // Ensure the sender isn't already married.
        if (PlayerSaveData.get((ServerWorld)sender.world, sender.getUuid()).isMarried()) {
            failMessage(sender, "You cannot send a proposal since you are already married or engaged.");
            return;
        }

        // Ensure the sender isn't himself.
        if (sender == receiver) {
            failMessage(sender, "You cannot propose to yourself.");
            return;
        }

        // Ensure the receiver hasn't already been proposed to by this player.
        if (hasProposalFrom(sender, receiver)) {
            failMessage(sender, "You have already sent a proposal to " + receiver.getEntityName());
        } else {
            // Send the proposal messages.
            successMessage(sender, "Your proposal to " + receiver.getEntityName() + " has been sent!");
            infoMessage(receiver, sender.getEntityName() + " has proposed marriage. To accept, type /mca accept " + sender.getEntityName());

            // Add the proposal to the receiver's proposal list.
            List<UUID> list = getProposalsFor(receiver);
            list.add(sender.getUuid());
            proposals.put(receiver.getUuid(), list);
        }
    }

    /**
     * Rejects and removes a proposal from the receiver to the sender.
     *
     * @param sender   The person rejecting the proposal.
     * @param receiver The initial proposer.
     */
    public void rejectProposal(PlayerEntity sender, PlayerEntity receiver) {
        // Ensure a proposal existed.
        if (!hasProposalFrom(receiver, sender)) {
            failMessage(sender, receiver.getDisplayName() + " hasn't proposed to you.");
        } else {
            // Notify of the proposal failure and remove it.
            successMessage(sender, "Your rejection has been sent.");
            failMessage(receiver, sender.getEntityName() + " rejected your proposal.");
            removeProposalFor(sender, receiver);
        }
    }

    /**
     * Accepts and removes a proposal from the receiver to the sender.
     *
     * @param sender   The person accepting the proposal.
     * @param receiver The initial proposer.
     */
    public void acceptProposal(PlayerEntity sender, PlayerEntity receiver) {
        // Ensure a proposal is active.
        if (!hasProposalFrom(receiver, sender)) {
            failMessage(sender, receiver.getEntityName() + " hasn't proposed to you.");
        } else {
            // Notify of acceptance.
            successMessage(receiver, sender.getEntityName() + " has accepted your proposal!");

            // Set both player datas as married.
            PlayerSaveData senderData = PlayerSaveData.get((ServerWorld)sender.world, sender.getUuid());
            PlayerSaveData receiverData = PlayerSaveData.get((ServerWorld)receiver.world, receiver.getUuid());
            senderData.marry(receiver.getUuid(), receiver.getEntityName(), MarriageState.MARRIED_TO_PLAYER);
            receiverData.marry(sender.getUuid(), sender.getEntityName(), MarriageState.MARRIED_TO_PLAYER);

            // Send success messages.
            successMessage(sender, "You and " + receiver.getEntityName() + " are now married.");
            successMessage(receiver, "You and " + sender.getEntityName() + " are now married.");

            // Remove the proposal.
            removeProposalFor(sender, receiver);
        }
    }

    /**
     * Ends the sender's marriage and notifies their spouse if the spouse is online.
     *
     * @param sender The person ending their marriage.
     */
    public void endMarriage(PlayerEntity sender) {
        // Retrieve all data instances and an instance of the ex-spouse if they are present.
        PlayerSaveData senderData = PlayerSaveData.get((ServerWorld)sender.world, sender.getUuid());

        // Ensure the sender is married
        if (!senderData.isMarried()) {
            failMessage(sender, "You are not married.");
            return;
        }

        // Lookup the spouse, if it's a villager, we can't continue

        if (senderData.getMarriageState() != MarriageState.MARRIED_TO_PLAYER) {
            failMessage(sender, "You cannot use this command when married to a villager.");
            return;
        }

        PlayerSaveData receiverData = PlayerSaveData.get((ServerWorld)sender.world, senderData.getSpouseUUID());

        // Notify the sender of the success and end both marriages.
        successMessage(sender, "Your marriage to " + senderData.getSpouseName() + " has ended.");
        PlayerEntity spouse = sender.world.getPlayerByUuid(senderData.getSpouseUUID());
        if (spouse != null) {
            failMessage(spouse, sender.getEntityName() + " has ended their marriage with you.");
        }

        senderData.endMarriage();
        receiverData.endMarriage();

        // Notify the ex if they are online.
    }

    /**
     * Initiates procreation with a married player.
     *
     * @param sender The person requesting procreation.
     */
    public void procreate(PlayerEntity sender) {
        // Ensure the sender is married.
        PlayerSaveData senderData = PlayerSaveData.get((ServerWorld)sender.world, sender.getUuid());
        if (!senderData.isMarried()) {
            failMessage(sender, "You cannot procreate if you are not married.");
            return;
        }

        // Ensure we don't already have a baby
        if (senderData.isBabyPresent()) {
            failMessage(sender, "You already have a baby.");
            return;
        }

        if (senderData.getMarriageState() != MarriageState.MARRIED_TO_PLAYER) {
            failMessage(sender, "You cannot use this command when married to a villager.");
            return;
        }
        // Ensure the spouse is online.
        PlayerEntity spouse = sender.world.getPlayerByUuid(senderData.getSpouseUUID());
        if (spouse != null) {
            // If the spouse is online and has previously sent a procreation request that hasn't expired, we can continue.
            // Otherwise we notify the spouse that they must also enter the command.
            if (!procreateMap.containsKey(spouse.getUuid())) {
                procreateMap.put(sender.getUuid(), System.currentTimeMillis() + 10000);
                infoMessage(spouse, sender.getEntityName() + " has requested procreation. To accept, type /mca procreate within 10 seconds.");
            } else {
                // On success, add a randomly generated baby to the original requester.
                successMessage(sender, "Procreation successful!");
                successMessage(spouse, "Procreation successful!");
                spouse.giveItemStack(new ItemStack(sender.world.getRandom().nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL));

                PlayerSaveData spouseData = PlayerSaveData.get((ServerWorld)spouse.world, spouse.getUuid());
                spouseData.setBabyPresent(true);
                senderData.setBabyPresent(true);
            }
        } else {
            failMessage(sender, "Your spouse is not present on the server.");
        }
    }

    private void successMessage(PlayerEntity player, String message) {
        player.sendSystemMessage(new LiteralText(message).formatted(Formatting.GREEN), Util.NIL_UUID);
    }

    private void failMessage(PlayerEntity player, String message) {
        player.sendSystemMessage(new LiteralText(message).formatted(Formatting.RED), Util.NIL_UUID);
    }

    private void infoMessage(PlayerEntity player, String message) {
        player.sendSystemMessage(new LiteralText(message).formatted(Formatting.YELLOW), Util.NIL_UUID);
    }

}

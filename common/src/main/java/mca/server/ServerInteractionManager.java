package mca.server;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import mca.Config;
import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.MarriageState;
import mca.network.client.ShowToastRequest;
import mca.server.world.data.BabyTracker;
import mca.server.world.data.PlayerSaveData;
import mca.util.compat.OptionalCompat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
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


    private ServerInteractionManager() {
    }

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

    public void onPlayerJoin(ServerPlayerEntity player) {
        PlayerSaveData playerData = PlayerSaveData.get((ServerWorld)player.world, player.getUuid());
        if (!playerData.isEntityDataSet()) {
            NetworkHandler.sendToPlayer(new ShowToastRequest(
                    "server.playerNotCustomized.title",
                    "server.playerNotCustomized.description"
            ), player);
        }
    }

    /**
     * Returns true if receiver has a proposal from sender.
     *
     * @param sender   Command sender
     * @param receiver Player whose name was entered by the sender
     *
     * @return boolean
     */
    private boolean hasProposalFrom(PlayerEntity sender, PlayerEntity receiver) {
        return getProposalsFor(receiver).contains(sender.getUuid());
    }

    /**
     * Returns all proposals for the provided player
     *
     * @param player Player whose proposals should be returned.
     *
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
            infoMessage(sender, new TranslatableText("server.noProposals"));
        } else {
            infoMessage(sender, new TranslatableText("server.proposals"));
        }

        // Send the name of all online players to the command sender.
        proposals.forEach((uuid -> {
            PlayerEntity player = sender.getEntityWorld().getPlayerByUuid(uuid);
            if (player != null) {
                infoMessage(sender, (BaseText)new LiteralText("- ").append(new LiteralText(player.getEntityName())));
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
        // Checks if the admin allows this
        if (!Config.getInstance().allowPlayerMarriage) {
            failMessage(sender, new TranslatableText("notify.playerMarriage.disabled"));
            return;
        }

        // Ensure the sender isn't already married.
        if (PlayerSaveData.get((ServerWorld)sender.world, sender.getUuid()).isMarried()) {
            failMessage(sender, new TranslatableText("server.alreadyMarried"));
            return;
        }

        // Ensure the sender isn't himself.
        if (sender == receiver) {
            failMessage(sender, new TranslatableText("server.proposedToYourself"));
            return;
        }

        // Ensure the receiver hasn't already been proposed to by this player.
        if (hasProposalFrom(sender, receiver)) {
            failMessage(sender, new TranslatableText("server.sentProposal", receiver.getEntityName()));
        } else {
            // Send the proposal messages.
            successMessage(sender, new TranslatableText("server.proposalSent", receiver.getEntityName()));
            infoMessage(receiver, new TranslatableText("server.proposedMarriage", sender.getEntityName()));

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
            failMessage(sender, new TranslatableText("server.noProposal", receiver.getDisplayName()));
        } else {
            // Notify of the proposal failure and remove it.
            successMessage(sender, new TranslatableText("server.proposalRejectionSent"));
            failMessage(receiver, new TranslatableText("server.proposalRejected", sender.getEntityName()));
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
            failMessage(sender, new TranslatableText("server.noProposal", receiver.getDisplayName()));
        } else {
            // Notify of acceptance.
            successMessage(receiver, new TranslatableText("server.proposalAccepted", receiver.getDisplayName()));

            // Set both player datas as married.
            PlayerSaveData.get((ServerWorld)sender.world, sender.getUuid()).marry(receiver);
            PlayerSaveData.get((ServerWorld)receiver.world, receiver.getUuid()).marry(sender);

            // Send success messages.
            successMessage(sender, new TranslatableText("server.married", receiver.getEntityName()));
            successMessage(receiver, new TranslatableText("server.married", sender.getEntityName()));

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
        EntityRelationship.of(sender).ifPresent(senderData -> {
            // Ensure the sender is married
            if (!senderData.isMarried()) {
                failMessage(sender, new TranslatableText("server.endMarriageNotMarried"));
                return;
            }

            // Lookup the spouse, if it's a villager, we can't continue
            if (senderData.getMarriageState() != MarriageState.MARRIED_TO_PLAYER) {
                failMessage(sender, new TranslatableText("server.marriedToVillager"));
                return;
            }

            // Notify the sender of the success and end both marriages.
            successMessage(sender, new TranslatableText("server.endMarriage", senderData.getSpouseName()));
            senderData.getSpouse().ifPresent(spouse -> {
                if (spouse instanceof PlayerEntity) {
                    // Notify the ex if they are online.
                    failMessage((PlayerEntity)spouse, new TranslatableText("server.marriageEnded", sender.getEntityName()));
                }
            });
            senderData.endMarriage(MarriageState.SINGLE);
            senderData.getSpouseUuid().map(id -> PlayerSaveData.get((ServerWorld)sender.world, id)).ifPresent(r -> r.endMarriage(MarriageState.SINGLE));
        });
    }

    /**
     * Initiates procreation with a married player.
     *
     * @param sender The person requesting procreation.
     */
    public void procreate(ServerPlayerEntity sender) {
        // Ensure the sender is married.
        PlayerSaveData senderData = PlayerSaveData.get(sender.getServerWorld(), sender.getUuid());
        if (!senderData.isMarried()) {
            failMessage(sender, new TranslatableText("server.notMarried"));
            return;
        }

        // Ensure the spouse is a player
        if (senderData.getMarriageState() != MarriageState.MARRIED_TO_PLAYER) {
            failMessage(sender, new TranslatableText("server.marriedToVillager"));
            return;
        }

        // Ensure we don't already have a baby
        BabyTracker.Pairing pairing = BabyTracker.get(sender.getServerWorld()).getPairing(sender.getUuid(), senderData.getSpouseUuid().orElse(null));
        if (pairing.getChildCount() > 0) {
            if (pairing.locateBaby(sender).getRight().wasFound()) {
                failMessage(sender, new TranslatableText("server.babyPresent"));
            } else {
                failMessage(sender, new TranslatableText("server.babyLost"));
                pairing.reconstructBaby(sender);
            }
            return;
        }

        // Ensure the spouse is online.
        OptionalCompat.ifPresentOrElse(senderData.getSpouse().filter(e -> e instanceof PlayerEntity).map(PlayerEntity.class::cast), spouse -> {
            // If the spouse is online and has previously sent a procreation request that hasn't expired, we can continue.
            // Otherwise we notify the spouse that they must also enter the command.
            if (!procreateMap.containsKey(spouse.getUuid())) {
                procreateMap.put(sender.getUuid(), System.currentTimeMillis() + 10000);
                infoMessage(spouse, new TranslatableText("server.procreationRequest", sender.getEntityName()));
            } else {
                // On success, add a randomly generated baby to the original requester.
                successMessage(sender, new TranslatableText("server.procreationSuccessful"));
                successMessage(spouse, new TranslatableText("server.procreationSuccessful"));

                pairing.addChild(s -> {
                    s.setGender(Gender.getRandom());
                    s.setOwner(sender);
                    spouse.giveItemStack(s.createItem());
                });
            }
        }, () -> failMessage(sender, new TranslatableText("server.spouseNotPresent")));
    }

    private void successMessage(PlayerEntity player, BaseText message) {
        player.sendSystemMessage(message.formatted(Formatting.GREEN), Util.NIL_UUID);
    }

    private void failMessage(PlayerEntity player, BaseText message) {
        player.sendSystemMessage(message.formatted(Formatting.RED), Util.NIL_UUID);
    }

    private void infoMessage(PlayerEntity player, BaseText message) {
        player.sendSystemMessage(message.formatted(Formatting.YELLOW), Util.NIL_UUID);
    }

}

package mca.entity.ai;

import mca.Config;
import mca.entity.EntityWrapper;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.resources.API;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public interface Messenger extends EntityWrapper {
    TargetPredicate CAN_RECEIVE = new TargetPredicate().ignoreEntityTargetRules();

    default boolean isSpeechImpaired() {
        return false;
    }

    default boolean isToYoungToSpeak() {
        return false;
    }

    default void playSpeechEffect() {

    }

    default DialogueType getDialogueType(PlayerEntity receiver) {
        return DialogueType.UNASSIGNED;
    }

    default TranslatableText getTranslatable(PlayerEntity target, String phraseId, Object... params) {
        String targetName;
        if (target.world instanceof ServerWorld) {
            //todo won't work on the few client side use cases
            targetName = FamilyTree.get((ServerWorld)target.world)
                    .getOrEmpty(target.getUuid())
                    .map(FamilyTreeNode::getName)
                    .filter(n -> !n.isEmpty())
                    .orElse(target.getName().getString());
        } else {
            targetName = target.getName().getString();
        }
        return new TranslatableText(getDialogueType(target).name() + "." + phraseId, targetName, params);
    }

    default void sendChatToAllAround(String phrase, Object... params) {
        for (PlayerEntity player : asEntity().world.getPlayers(CAN_RECEIVE, asEntity(), asEntity().getBoundingBox().expand(20))) {
            float dist = player.distanceTo(asEntity());
            sendChatMessage(getTranslatable(player, phrase, params).formatted(dist < 10 ? Formatting.WHITE : Formatting.GRAY), player);
        }
    }

    default void sendChatMessage(PlayerEntity target, String phraseId, Object... params) {
        sendChatMessage(getTranslatable(target, phraseId, params), target);
    }

    default void sendChatMessage(MutableText message, Entity receiver) {
        // Infected villagers do not speak
        if (isSpeechImpaired()) {
            message = new TranslatableText(API.getRandomSentence("zombie", message.getString()));
        } else if (isToYoungToSpeak()) {
            message = new TranslatableText(API.getRandomSentence("baby", message.getString()));
        }

        receiver.sendSystemMessage(new LiteralText(Config.getInstance().villagerChatPrefix).append(asEntity().getDisplayName()).append(": ").append(message), receiver.getUuid());

        playSpeechEffect();
    }

    default void sendEventMessage(Text message, PlayerEntity receiver) {
        receiver.sendMessage(message, true);
    }

    default void sendEventMessage(Text message) {
        if (!(this instanceof Entity)) {
            return; // Can't tell all
        }
        sendEventMessage(((Entity)this).world, message);
    }

    static void sendEventMessage(World world, Text message) {
        world.getPlayers().forEach(player -> player.sendMessage(message, true));
    }
}

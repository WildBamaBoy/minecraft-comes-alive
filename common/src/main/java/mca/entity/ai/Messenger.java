package mca.entity.ai;

import mca.Config;
import mca.entity.EntityWrapper;
import mca.resources.API;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import java.util.stream.Stream;

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
        return new TranslatableText(getDialogueType(target).getTranslationKey(phraseId), Stream.concat(Stream.of(target.getName()), Stream.of(params)).toArray());
    }

    default void sendChatToAllAround(String phrase, Object...params) {
        for (PlayerEntity player : asEntity().world.getPlayers(CAN_RECEIVE, asEntity(), asEntity().getBoundingBox().expand(20))) {
            sendChatMessage(player, phrase, params);
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
        sendEventMessage(((Entity) this).world, message);
    }

    static void sendEventMessage(World world, Text message) {
        world.getPlayers().forEach(player -> player.sendMessage(message, true));
    }
}

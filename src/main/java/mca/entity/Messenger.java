package mca.entity;

import java.util.stream.Stream;

import mca.core.MCA;
import mca.enums.DialogueType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public interface Messenger {

    default Entity asEntity() {
        return (Entity)this;
    }

    default boolean isInfected() {
        return false;
    }

    default void playSpeechEffect() {

    }

    default DialogueType getDialogueType(PlayerEntity receiver) {
        return DialogueType.UNASSIGNED;
    }

    default void sendChatMessage(PlayerEntity target, String phraseId, Object... params) {
        sendChatMessage(new TranslatableText(getDialogueType(target).getTranslationKey(phraseId), Stream.concat(Stream.of(target.getName()), Stream.of(params)).toArray()), target);
    }

    default void sendChatMessage(MutableText message, Entity receiver) {
        // Infected villagers do not speak
        if (isInfected()) {
            message = message.formatted(Formatting.OBFUSCATED);
        }

        receiver.sendSystemMessage(new LiteralText(MCA.getConfig().villagerChatPrefix).append(asEntity().getDisplayName()).append(": ").append(message), receiver.getUuid());

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

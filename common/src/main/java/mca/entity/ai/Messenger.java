package mca.entity.ai;

import mca.Config;
import mca.resources.API;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public interface Messenger {

    default Entity asEntity() {
        return (Entity) this;
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
        if (isInfected() || true) {
            String str = message.getString();
            int wordCount = str.split(" ").length;

            // create zombie sentence
            List<String> words = new LinkedList<>();
            for (int i = 0; i < wordCount; i++) {
                words.add(API.getRandomZombieWord());
            }
            String concat = String.join(" ", words);

            // add !?.
            char last = str.charAt(str.length() - 1);
            if (last == '!' || last == '?' || last == '.') {
                concat += last;
            }
            
            message = new TranslatableText(concat);
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

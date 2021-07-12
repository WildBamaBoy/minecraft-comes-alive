package mca.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public interface Messenger {

    default void sendMessageTo(String message, Entity receiver) {
        if (receiver instanceof PlayerEntity) {
            ((PlayerEntity)receiver).sendMessage(new TranslatableText(message), true);
        } else {
            receiver.sendSystemMessage(new TranslatableText(message), receiver.getUuid());
        }
    }

    default Text formatDialogueMessage(String phraseId, PlayerEntity receiver, Object... params) {
        return new TranslatableText(phraseId);
    }

    default void say(PlayerEntity target, String phraseId, Object... params) {
        target.sendMessage(formatDialogueMessage(phraseId, target), true);
    }

    default void tellAll(Text message) {
        if (!(this instanceof Entity)) {
            return; // Can't tell all
        }
        tellAll(((Entity)this).world, message);
    }

    static void tellAll(World world, Text message) {
        world.getPlayers().forEach(player -> player.sendMessage(message, true));
    }
}

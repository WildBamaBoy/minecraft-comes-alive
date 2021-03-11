package com.minecraftcomesalive.mca.interaction.base;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import com.minecraftcomesalive.mca.api.API;
import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;

public class GiftInteraction implements IInteraction {
    @Override
    public void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        CItemStack heldItem = player.getHeldItem(CEnumHand.MAIN_HAND);
        int giftValue = API.getGiftValueFromStack(heldItem);
        if (giftValue > 0) {
            memories.modHearts((int) Math.max(1, Math.round(giftValue - (memories.getInteractionFatigue() * (giftValue * 0.25)))));

            if (giftValue > 20) {
                villager.say(player, "gift.best");
            } else if (giftValue > 10) {
                villager.say(player, "gift.better");
            } else {
                villager.say(player, "gift.good");
            }
        } else {
            memories.modHearts(giftValue);
            villager.say(player, "gift.bad");
        }

        memories.modInteractionFatigue(1);

        if (!player.isCreativeMode()) {
            heldItem.decrStackSize();
        }
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        return true;
    }
}

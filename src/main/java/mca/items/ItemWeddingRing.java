package mca.items;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerHistory;
import mca.entity.data.PlayerSaveData;
import net.minecraft.entity.player.EntityPlayer;

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(player);
        PlayerHistory history = villager.getPlayerHistoryFor(player.getUniqueID());
        String response;

        if (villager.isMarriedTo(player.getUniqueID())) {
            response = "interaction.marry.fail.marriedtogiver";
        } else if (villager.isMarried()) {
            response = "interaction.marry.fail.marriedtoother";
        } else if (playerData.isMarriedOrEngaged()) {
            response = "interaction.marry.fail.marriedtoother";
        } else if (history.getHearts() < MCA.getConfig().marriageHeartsRequirement) {
            response = "interaction.marry.fail.lowhearts";
        } else {
            response = "interaction.marry.success";
            playerData.marry(villager.getUniqueID(), villager.get(EntityVillagerMCA.VILLAGER_NAME));
            villager.marry(player);
        }

        villager.say(player, response);
        return false;
    }
}

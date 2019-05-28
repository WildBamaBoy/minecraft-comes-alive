package mca.items;

import com.google.common.base.Optional;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerHistory;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
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
            villager.getPlayerHistoryFor(player.getUniqueID()).setDialogueType(EnumDialogueType.SPOUSE);
            villager.marry(player);
        }

        villager.say(Optional.of(player), response);
        return false;
    }
}

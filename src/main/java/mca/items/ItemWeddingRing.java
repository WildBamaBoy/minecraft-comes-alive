package mca.items;

import cobalt.minecraft.entity.player.CPlayer;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public boolean handle(CPlayer player, EntityVillagerMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(player.getWorld(), player.getUUID());
        Memories memory = villager.getMemoriesForPlayer(player);
        String response;

        if (villager.isMarriedTo(player.getUUID()))
            response = "interaction.marry.fail.marriedtogiver";
        else if (villager.isMarried())
            response = "interaction.marry.fail.marriedtoother";
        else if (playerData.isMarried())
            response = "interaction.marry.fail.marriedtoother";
        else if (this instanceof ItemEngagementRing && memory.getHearts() < MCA.getConfig().marriageHeartsRequirement / 2)
            response = "interaction.marry.fail.lowhearts";
        else if (!(this instanceof ItemEngagementRing) && memory.getHearts() < MCA.getConfig().marriageHeartsRequirement)
            response = "interaction.marry.fail.lowhearts";
        else {
            response = "interaction.marry.success";
            playerData.marry(villager.getUUID(), villager.get(EntityVillagerMCA.villagerName));
            villager.getMemoriesForPlayer(player).setDialogueType(EnumDialogueType.SPOUSE);
            villager.marry(player);
            villager.modifyMoodLevel(15);
        }

        villager.say(player, response);
        return false;
    }
}

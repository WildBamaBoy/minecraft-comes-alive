package mca.items;

import cobalt.minecraft.world.CWorld;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public ItemWeddingRing(Item.Properties properties) {
        super(properties);
    }

    public boolean handle(PlayerEntity player, EntityVillagerMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(CWorld.fromMC(player.level), player.getUUID());
        Memories memory = villager.getMemoriesForPlayer(player);
        String response;
        boolean consume = false;

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
            playerData.marry(villager.getUUID(), villager.villagerName.get());
            villager.getMemoriesForPlayer(player).setDialogueType(EnumDialogueType.SPOUSE);
            villager.marry(player);
            villager.modifyMoodLevel(15);
            consume = true;
        }

        villager.say(player, response);
        return consume;
    }
}

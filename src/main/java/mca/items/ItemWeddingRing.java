package mca.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public ItemWeddingRing(Item.Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//        PlayerSaveData playerData = PlayerSaveData.get(player.getWorld(), player.getUUID());
//        Memories memory = villager.getMemoriesForPlayer(player);
//        String response;
//
//        if (villager.isMarriedTo(player.getUUID()))
//            response = "interaction.marry.fail.marriedtogiver";
//        else if (villager.isMarried())
//            response = "interaction.marry.fail.marriedtoother";
//        else if (playerData.isMarried())
//            response = "interaction.marry.fail.marriedtoother";
//        else if (this instanceof ItemEngagementRing && memory.getHearts() < MCA.getConfig().marriageHeartsRequirement / 2)
//            response = "interaction.marry.fail.lowhearts";
//        else if (!(this instanceof ItemEngagementRing) && memory.getHearts() < MCA.getConfig().marriageHeartsRequirement)
//            response = "interaction.marry.fail.lowhearts";
//        else {
//            response = "interaction.marry.success";
//            playerData.marry(villager.getUUID(), villager.get(EntityVillagerMCA.villagerName));
//            villager.getMemoriesForPlayer(player).setDialogueType(EnumDialogueType.SPOUSE);
//            villager.marry(player);
//            villager.modifyMoodLevel(15);
//        }
//
//        villager.say(player, response);
        return null;
    }
}

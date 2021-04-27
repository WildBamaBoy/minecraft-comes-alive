package mca.items;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public ItemWeddingRing(Properties properties) {
        super(properties);
    }

    public ActionResult<ItemStack> handleRightClick(CWorld worldIn, CPlayer playerIn, CEnumHand hand) {
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

    @Override
    public ActionResultType handleUseOnBlock(CItemUseContext context) {
        return null;
    }

    @Override
    public ActionResultType update(CItemStack itemStack, CWorld world, CEntity entity) {
        return null;
    }
}

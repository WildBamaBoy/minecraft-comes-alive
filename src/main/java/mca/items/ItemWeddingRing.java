package mca.items;

import cobalt.minecraft.world.CWorld;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public ItemWeddingRing(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Marry someone with this if you meet the heart requirements."));
    }

    public boolean handle(PlayerEntity player, EntityVillagerMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(CWorld.fromMC(player.level), player.getUUID());
        Memories memory = villager.getMemoriesForPlayer(player);
        String response;
        boolean consume = false;

        if (villager.isBaby())
            response = "interaction.marry.fail.isbaby";
        else if (villager.playerIsParent(player))
            response = "interaction.marry.fail.isparent";
        else if (villager.isMarriedTo(player.getUUID()))
            response = "interaction.marry.fail.marriedtogiver";
        else if (villager.isMarried())
            response = "interaction.marry.fail.marriedtoother";
        else if (playerData.isMarried())
            response = "interaction.marry.fail.playermarried";
        else if (memory.getHearts() < MCA.getConfig().marriageHeartsRequirement)
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

package mca.items;

import mca.core.MCA;
import mca.entity.Relationship;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.DialogueType;
import mca.enums.MarriageState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class WeddingRingItem extends Item implements SpecialCaseGift {
    public WeddingRingItem(Item.Settings properties) {
        super(properties);
    }

    protected int getHeartsRequired() {
        return MCA.getConfig().marriageHeartsRequirement;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        // TODO: Translation string
        tooltip.add(new LiteralText("Halves the hearts required to marry someone."));
    }

    @Override
    public boolean handle(PlayerEntity player, VillagerEntityMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(player.world, player.getUuid());
        Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);
        String response;
        boolean consume = false;

        if (villager.isBaby()) {
            response = "interaction.marry.fail.isbaby";
        } else if (Relationship.IS_PARENT.test(villager, player)) {
            response = "interaction.marry.fail.isparent";
        } else if (Relationship.IS_MARRIED.test(villager, player)) {
            response = "interaction.marry.fail.marriedtogiver";
        } else if (villager.getRelationships().isMarried()) {
            response = "interaction.marry.fail.marriedtoother";
        } else if (playerData.isMarried()) {
            response = "interaction.marry.fail.playermarried";
        } else if (memory.getHearts() < getHeartsRequired()) {
            response = "interaction.marry.fail.lowhearts";
        } else {
            response = "interaction.marry.success";
            playerData.marry(villager.getUuid(), villager.villagerName.get(), MarriageState.MARRIED);
            villager.getVillagerBrain().getMemoriesForPlayer(player).setDialogueType(DialogueType.SPOUSE);
            villager.getRelationships().marry(player);
            villager.getVillagerBrain().modifyMoodLevel(15);
            consume = true;
        }

        villager.sendChatMessage(player, response);
        return consume;
    }
}

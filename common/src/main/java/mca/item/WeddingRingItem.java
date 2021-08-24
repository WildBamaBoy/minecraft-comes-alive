package mca.item;

import mca.Config;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.DialogueType;
import mca.entity.ai.Memories;
import mca.entity.ai.Relationship;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class WeddingRingItem extends TooltippedItem implements SpecialCaseGift {

    private final float heartsModifier;

    public WeddingRingItem(Item.Settings properties) {
        this(properties, 1);
    }

    public WeddingRingItem(Item.Settings properties, float modifier) {
        super(properties);
        heartsModifier = modifier;
    }

    protected float getHeartsRequired() {
        return Config.getInstance().marriageHeartsRequirement * heartsModifier;
    }

    @Override
    public boolean handle(ServerPlayerEntity player, VillagerEntityMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get((ServerWorld)player.world, player.getUuid());
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
            playerData.marry(villager);
            villager.getRelationships().marry(player);
            villager.getVillagerBrain().modifyMoodLevel(15);
            consume = true;
        }

        villager.sendChatMessage(player, response);
        return consume;
    }
}

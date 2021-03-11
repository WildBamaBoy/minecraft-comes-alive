package com.minecraftcomesalive.mca.interaction.main;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;
import com.minecraftcomesalive.mca.enums.EnumAgeState;
import com.minecraftcomesalive.mca.enums.EnumMoveState;
import net.minecraft.util.text.StringTextComponent;

public class TradeInteraction implements IInteraction {
    @Override
    public void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        villager.openMerchantContainer(player.getMcPlayer(),
                new StringTextComponent(villager.getNameForDisplay()),
                villager.getVillagerData().getLevel());
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        if (villager.getAgeState() != EnumAgeState.ADULT || villager.playerIsParent(player) || villager.playerIsSpouse(player)) {
            return false;
        }

        return true;
    }
}

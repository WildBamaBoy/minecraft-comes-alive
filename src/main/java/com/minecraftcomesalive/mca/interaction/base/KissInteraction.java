package com.minecraftcomesalive.mca.interaction.base;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.enums.EnumAgeState;

public class KissInteraction extends AbstractBaseInteraction {
    @Override
    public int getHeartsModifier() {
        return 15;
    }

    @Override
    public float getBaseSuccessChance() {
        return 50;
    }

    @Override
    public String getLangPrefix() {
        return "kiss";
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        if (villager.getAgeState() != EnumAgeState.ADULT || villager.playerIsParent(player)) {
            return false;
        }

        return true;
    }
}

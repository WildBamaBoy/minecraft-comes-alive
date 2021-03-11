package com.minecraftcomesalive.mca.interaction.base;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.enums.EnumAgeState;

public class HugInteraction extends AbstractBaseInteraction {
    @Override
    public int getHeartsModifier() {
        return 12;
    }

    @Override
    public float getBaseSuccessChance() {
        return 60;
    }

    @Override
    public String getLangPrefix() {
        return "hug";
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        if (villager.getAgeState() != EnumAgeState.ADULT || villager.playerIsParent(player)) {
            return false;
        }

        return true;
    }
}

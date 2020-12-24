package com.minecraftcomesalive.mca.interaction.main;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;
import com.minecraftcomesalive.mca.enums.EnumAgeState;
import net.minecraft.entity.passive.horse.HorseEntity;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class ProcreateInteraction implements IInteraction {
    @Override
    public void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        villager.setIsProcreating(true);
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        if (villager.playerIsSpouse(player) && villager.getAgeState() == EnumAgeState.ADULT) {
            return true;
        }
        return false;
    }
}

package com.minecraftcomesalive.mca.interaction.main;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;
import com.minecraftcomesalive.mca.enums.EnumAgeState;
import net.minecraft.util.text.StringTextComponent;

public class GoHomeInteraction implements IInteraction {
    @Override
    public void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        villager.goHome(player);
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        return true;
    }
}

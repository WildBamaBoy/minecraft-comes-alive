package com.minecraftcomesalive.mca.api;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;

public interface IInteraction {
    void run(EntityVillagerMCA villager, Memories memories, CPlayer player);
    boolean isValidFor(EntityVillagerMCA villager, CPlayer player);
}

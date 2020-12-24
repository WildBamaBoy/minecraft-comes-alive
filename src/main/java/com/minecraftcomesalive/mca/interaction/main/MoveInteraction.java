package com.minecraftcomesalive.mca.interaction.main;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;
import com.minecraftcomesalive.mca.enums.EnumMoveState;

public class MoveInteraction implements IInteraction {
    @Override
    public void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        villager.setMoveState(EnumMoveState.MOVE);
        villager.setFollowingPlayer(null);
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        if (villager.getParents().hasPlayerParent(villager.world) && !villager.playerIsParent(player)) {
            return false;
        }

        return true;
    }
}

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

public class RideHorseInteraction implements IInteraction {
    @Override
    public void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        if (villager.getRidingEntity() != null) {
            villager.stopRiding();
        } else {
            try {
                List<HorseEntity> horses = villager.world.getEntities(HorseEntity.class, h -> (h.isHorseSaddled() && !h.isBeingRidden() && h.getDistance(this) < 3.0D));
                villager.startRiding(horses.stream().min(Comparator.comparingDouble(villager::getDistance)).get(), true);
                villager.getNavigator().clearPath();
            } catch (NoSuchElementException e) {
                villager.say(player, "interaction.ridehorse.fail.notnearby");
            }
        }
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        if (villager.getAgeState() != EnumAgeState.ADULT && !villager.playerIsParent(player)) {
            return false;
        }
        return true;
    }
}

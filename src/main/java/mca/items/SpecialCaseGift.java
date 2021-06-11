package mca.items;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.player.PlayerEntity;

public interface SpecialCaseGift {

    boolean handle(PlayerEntity player, VillagerEntityMCA villager);
}

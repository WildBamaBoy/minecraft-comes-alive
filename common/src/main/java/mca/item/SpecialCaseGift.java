package mca.item;

import mca.entity.VillagerEntityMCA;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SpecialCaseGift {

    boolean handle(ServerPlayerEntity player, VillagerEntityMCA villager);
}

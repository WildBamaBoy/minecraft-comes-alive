package mca.core.minecraft;

import mca.core.MCA;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class MemoryModuleTypeMCA {
    //if you do not provide a codec, it does not save! however, for things like players, you will likely need to save their UUID beforehand.
    public static final MemoryModuleType<PlayerEntity> PLAYER_FOLLOWING = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<Boolean> STAYING = new MemoryModuleType<>(Optional.empty());

    public static void init() {
        PLAYER_FOLLOWING.setRegistryName(new ResourceLocation(MCA.MOD_ID + "player_following_memory"));
        ForgeRegistries.MEMORY_MODULE_TYPES.register(PLAYER_FOLLOWING);
        STAYING.setRegistryName(new ResourceLocation(MCA.MOD_ID + "staying_memory"));
        ForgeRegistries.MEMORY_MODULE_TYPES.register(STAYING);
    }
}

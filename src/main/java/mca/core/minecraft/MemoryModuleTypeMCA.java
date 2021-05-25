package mca.core.minecraft;

import com.mojang.serialization.Codec;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;

public class MemoryModuleTypeMCA {
    //if you do not provide a codec, it does not save! however, for things like players, you will likely need to save their UUID beforehand.
    public static final MemoryModuleType<PlayerEntity> PLAYER_FOLLOWING = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<Boolean> STAYING = new MemoryModuleType<>(Optional.of(Codec.BOOL));

    public static void init() {
        Registration.MEMORY_MODULE_TYPES.register("player_following_memory", () -> PLAYER_FOLLOWING);
        Registration.MEMORY_MODULE_TYPES.register("staying_memory", () -> STAYING);
    }
}

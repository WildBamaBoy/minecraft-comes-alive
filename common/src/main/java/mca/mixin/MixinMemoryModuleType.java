package mca.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.serialization.Codec;

import net.minecraft.entity.ai.brain.MemoryModuleType;

@Mixin(MemoryModuleType.class)
public interface MixinMemoryModuleType {
    @Invoker("<init>")
    static <U> MemoryModuleType<U> init(Optional<Codec<U>> codec) {
        return null;
    }
}

package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.serialization.Codec;

import net.minecraft.entity.ai.brain.MemoryModuleType;

@Mixin(MemoryModuleType.class)
public interface MixinMemoryModuleType {
    @Invoker("register")
    static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return null;
    }

    @Invoker("register")
    static <U> MemoryModuleType<U> register(String id) {
        return null;
    }
}

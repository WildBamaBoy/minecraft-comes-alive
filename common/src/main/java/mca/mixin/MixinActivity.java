package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.ai.brain.Activity;

@Mixin(Activity.class)
public interface MixinActivity {
    @Invoker("register")
    static Activity register(String id) {
        return null;
    }
}

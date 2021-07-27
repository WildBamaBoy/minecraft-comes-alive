package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

@Mixin(Criteria.class)
public interface MixinCriteria {
    @Invoker("register")
    static <T extends Criterion<?>> T register(T object) {
        return null;
    }
}

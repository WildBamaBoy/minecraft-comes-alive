package mca.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.entity.Infectable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

@Mixin(TargetPredicate.class)
abstract class MixinTargetPredicate {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    public void onTest(@Nullable LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> info) {
        if (targetEntity instanceof Infectable && !((Infectable)targetEntity).canBeTargetedBy(baseEntity)) {
            info.setReturnValue(false);
        }
    }
}

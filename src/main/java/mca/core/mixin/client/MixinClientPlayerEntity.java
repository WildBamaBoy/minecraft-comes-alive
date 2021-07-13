package mca.core.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.items.BabyItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(ClientPlayerEntity.class)
abstract class MixinClientPlayerEntity extends LivingEntity {
    private MixinClientPlayerEntity() { super(null, null); }

    @Inject(method = "dropSelectedItem(Z)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void onDropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> info) {
        ItemStack stack = this.getMainHandStack();
        if (stack.getItem() instanceof BabyItem && !((BabyItem)stack.getItem()).onDropped(stack, (PlayerEntity)(Object)this)) {
            info.setReturnValue(false);
        }
    }
}

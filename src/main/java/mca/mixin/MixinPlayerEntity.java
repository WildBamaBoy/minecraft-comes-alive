package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.item.BabyItem;
import mca.server.world.data.VillageManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity {
    private MixinPlayerEntity() { super(null, null); }

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onOnDeath(DamageSource cause, CallbackInfo info) {
        if (!world.isClient) {
            VillageManager.get((ServerWorld)world).getBabyBunker().push((PlayerEntity)(Object)this);
        }
    }

    @Inject(method = "dropSelectedItem(Z)Z",
            at = @At("HEAD"),
            cancellable = true)
    public void onDropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> info) {
        ItemStack stack = this.getMainHandStack();
        if (stack.getItem() instanceof BabyItem && !((BabyItem)stack.getItem()).onDropped(stack, (PlayerEntity)(Object)this)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true)
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> info) {
        if (stack.getItem() instanceof BabyItem && !((BabyItem)stack.getItem()).onDropped(stack, (PlayerEntity)(Object)this)) {
            info.setReturnValue(null);
        }
    }
}

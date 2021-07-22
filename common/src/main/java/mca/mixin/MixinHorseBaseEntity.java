package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.inventory.InventoryChangedListener;

@Mixin(HorseBaseEntity.class)
abstract class MixinHorseBaseEntity extends AnimalEntity implements InventoryChangedListener, JumpingMount, Saddleable {
    MixinHorseBaseEntity() { super(null, null); }

    @Inject(method = "isImmobile()Z", at = @At("HEAD"), cancellable = true)
    private void onIsImmobile(CallbackInfoReturnable<Boolean> info) {
        if (getPrimaryPassenger() instanceof VillagerEntityMCA) {
            info.setReturnValue(false); // Fixes villagers not being able to move when riding a horse
        }
    }
}

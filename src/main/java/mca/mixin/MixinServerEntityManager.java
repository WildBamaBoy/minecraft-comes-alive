package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.server.SpawnQueue;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;

@Mixin(ServerEntityManager.class)
abstract class MixinServerEntityManager<T extends EntityLike> implements AutoCloseable {
    @Inject(method = "addEntityUuid(Lnet/minecraft/world/entity/EntityLike;)Z", at = @At("HEAD"), cancellable = true)
    private void onAddEntityUuid(T entity, CallbackInfoReturnable<Boolean> info) {

        if (SpawnQueue.getInstance().addVillager(entity)) {
            info.setReturnValue(false);
        }
    }
}

package mca.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.ducks.IVillagerEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

@Mixin(VillagerEntity.class)
abstract class MixinVillagerEntity implements IVillagerEntity {

    @Nullable
    private transient SpawnReason reason;

    @Override
    public SpawnReason getSpawnReason() {
        return reason == null ? SpawnReason.NATURAL : reason;
    }

    @Inject(method = "initialize", at = @At("HEAD"))
    private void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
            @Nullable EntityData entityData,
            @Nullable NbtCompound entityNbt, CallbackInfoReturnable<EntityData> info) {
        reason = spawnReason;
    }
}

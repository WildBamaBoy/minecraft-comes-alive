package mca.mixin;

import mca.ducks.IVillagerEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillagerEntity.class)
abstract class MixinZombieVillagerEntity implements IVillagerEntity {

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

    @ModifyVariable(method = "setVillagerData", at = @At("HEAD"), ordinal = 0)
    private VillagerData setVillagerData(VillagerData villagerData) {
        VillagerProfession profession = villagerData.getProfession();
        if (profession.toString().startsWith("mca.")) {
            villagerData = villagerData.withProfession(VillagerProfession.NONE);
        }
        return villagerData;
    }
}

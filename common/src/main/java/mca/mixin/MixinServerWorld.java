package mca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mca.server.SpawnQueue;
import mca.server.world.data.VillageManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;

@Mixin(ServerWorld.class)
abstract class MixinServerWorld extends World implements StructureWorldAccess {
    MixinServerWorld() { super(null, null, null, null, true, false, 0);}

    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (SpawnQueue.getInstance().addVillager(entity)) {
            info.setReturnValue(false);
        }
    }
    @Inject(method = "onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V",
            at = @At("HEAD")
    )
    public void onOnBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo info) {
        if (oldBlock.getBlock() != newBlock.getBlock()) {
            VillageManager.get((ServerWorld)(Object)this).getReaperSpawner().trySpawnReaper(this, newBlock, pos);
        }
    }
}

@Mixin(ProtoChunk.class)
abstract class MixinProtoChunk implements Chunk {
    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onAddEntity(Entity entity, CallbackInfo info) {
        if (SpawnQueue.getInstance().addVillager(entity)) {
            info.cancel();
        }
    }
}
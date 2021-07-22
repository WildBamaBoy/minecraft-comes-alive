package mca.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.nbt.NbtCompound;

public class TombstoneBlockData extends TombstoneBlock.Data implements BlockEntityClientSerializable {
    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    @Override
    public void sync() {
        BlockEntityClientSerializable.super.sync();
    }

    public static void bootstrap() {
        TombstoneBlock.Data.constructor = TombstoneBlockData::new;
    }
}

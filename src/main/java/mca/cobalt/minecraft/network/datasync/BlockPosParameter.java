package mca.cobalt.minecraft.network.datasync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class BlockPosParameter extends CDataParameter<BlockPos> {
    private final DataTracker data;
    private final BlockPos defaultValue;

    public BlockPosParameter(String id, Class<? extends Entity> e, DataTracker d, BlockPos dv) {
        super(id, e, TrackedDataHandlerRegistry.BLOCK_POS);
        data = d;
        defaultValue = dv;
    }

    public BlockPos get() {
        return data.get(param);
    }

    public void set(BlockPos v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.startTracking(param, defaultValue);
    }

    @Override
    public void load(NbtCompound nbt) {
        set(getBlockPos(nbt, id));
    }

    @Override
    public void save(NbtCompound nbt) {
        putBlockPos(nbt, id, get());
    }


    public static BlockPos getBlockPos(NbtCompound tag, String key) {
        return new BlockPos(
                tag.getInt(key + "X"),
                tag.getInt(key + "Y"),
                tag.getInt(key + "Z")
        );
    }

    public static void putBlockPos(NbtCompound tag, String key, BlockPos pos) {
        tag.putInt(key + "X", pos.getX());
        tag.putInt(key + "Y", pos.getY());
        tag.putInt(key + "Z", pos.getZ());
    }

}

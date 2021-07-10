package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
    public void load(CNBT nbt) {
        set(nbt.getBlockPos(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setBlockPos(id, get());
    }
}

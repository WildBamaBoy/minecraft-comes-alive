package cobalt.minecraft.network.datasync;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.util.math.CPos;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;

public class CPosParameter extends CDataParameter {
    private final DataParameter<BlockPos> param;
    private final EntityDataManager data;
    private final String id;
    private final CPos defaultValue;

    public CPosParameter(String id, Class<? extends Entity> e, EntityDataManager d, CPos dv) {
        this.id = id;
        param = EntityDataManager.defineId(e, DataSerializers.BLOCK_POS);
        data = d;
        defaultValue = dv;
    }

    public CPos get() {
        return CPos.fromMC(data.get(param));
    }

    public void set(CPos v) {
        data.set(param, v.getMcPos());
    }

    @Override
    public void register() {
        data.define(param, defaultValue.getMcPos());
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getCPos(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setCPos(id, get());
    }
}

package mca.api.cobalt.minecraft.network.datasync;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.core.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CDataManager {
    private final Entity entity;
    private final Map<String, CDataParameter> params;

    public CDataManager(Entity e) {
        entity = e;
        params = new HashMap<>();
    }

    public CFloatParameter newFloat(String id) {
        return newFloat(id, 0.0f);
    }

    public CFloatParameter newFloat(String id, float defaultValue) {
        CFloatParameter p = new CFloatParameter(id, entity.getClass(), entity.getEntityData(), defaultValue);
        params.put(id, p);
        return p;
    }

    public CIntegerParameter newInteger(String id) {
        return newInteger(id, 0);
    }

    public CIntegerParameter newInteger(String id, int defaultValue) {
        CIntegerParameter p = new CIntegerParameter(id, entity.getClass(), entity.getEntityData(), defaultValue);
        params.put(id, p);
        return p;
    }

    public CBooleanParameter newBoolean(String id) {
        return newBoolean(id, false);
    }

    public CBooleanParameter newBoolean(String id, boolean defaultValue) {
        CBooleanParameter p = new CBooleanParameter(id, entity.getClass(), entity.getEntityData(), defaultValue);
        params.put(id, p);
        return p;
    }

    public CUUIDParameter newUUID(String id) {
        return newUUID(id, Constants.ZERO_UUID);
    }

    public CUUIDParameter newUUID(String id, UUID defaultValue) {
        CUUIDParameter p = new CUUIDParameter(id, entity.getClass(), entity.getEntityData(), defaultValue);
        params.put(id, p);
        return p;
    }

    public BlockPosParameter newPos(String id) {
        return newPos(id, BlockPos.ZERO);
    }

    public BlockPosParameter newPos(String id, BlockPos defaultValue) {
        BlockPosParameter p = new BlockPosParameter(id, entity.getClass(), entity.getEntityData(), defaultValue);
        params.put(id, p);
        return p;
    }

    public CStringParameter newString(String id) {
        return newString(id, "");
    }

    public CStringParameter newString(String id, String defaultValue) {
        CStringParameter p = new CStringParameter(id, entity.getClass(), entity.getEntityData(), defaultValue);
        params.put(id, p);
        return p;
    }

    public CTagParameter newTag(String id) {
        CTagParameter p = new CTagParameter(id, entity.getClass(), entity.getEntityData());
        params.put(id, p);
        return p;
    }

    //register all entries
    public void register() {
        for (CDataParameter p : params.values()) {
            p.register();
        }
    }

    //load entity from nbt
    public void load(CNBT nbt) {
        for (CDataParameter p : params.values()) {
            p.load(nbt);
        }
    }

    //save entity from nbt
    public void save(CNBT nbt) {
        for (CDataParameter p : params.values()) {
            p.save(nbt);
        }
    }
}

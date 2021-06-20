package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;

import java.util.HashMap;
import java.util.Map;

abstract public class CDataParameter<T> {
    private final static Map<Class<? extends Entity>, Map<String, Object>> params = new HashMap<>();
    protected final String id;
    protected final DataParameter<T> param;

    protected CDataParameter(String id, Class<? extends Entity> e, IDataSerializer<T> s) {
        this.id = id;

        if (!params.containsKey(e)) {
            params.put(e, new HashMap<>());
        }

        Map<String, Object> m = params.get(e);
        if (!m.containsKey(id)) {
            m.put(id, EntityDataManager.defineId(e, s));
        }

        param = (DataParameter<T>) m.get(id);
    }

    public abstract void register();

    public abstract void load(CNBT nbt);

    public abstract void save(CNBT nbt);

    public DataParameter<T> getParam() {
        return param;
    }
}

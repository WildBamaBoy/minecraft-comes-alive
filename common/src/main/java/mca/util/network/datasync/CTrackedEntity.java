package mca.util.network.datasync;

import net.minecraft.entity.Entity;

public interface CTrackedEntity<T extends Entity & CTrackedEntity<T>> {

    CDataManager<T> getTypeDataManager();

    @SuppressWarnings("unchecked")
    default <P, TrackedP> void setTrackedValue(CParameter<P, TrackedP> key, P value) {
        getTypeDataManager().set((T)this, key, value);
    }

    @SuppressWarnings("unchecked")
    default <P, TrackedP> P getTrackedValue(CParameter<P, TrackedP> key) {
        return getTypeDataManager().get((T)this, key);
    }
}

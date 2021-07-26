package mca.entity;

import net.minecraft.entity.mob.MobEntity;

public interface EntityWrapper {
    default MobEntity asEntity() {
        return (MobEntity) this;
    }
}

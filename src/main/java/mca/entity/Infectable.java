package mca.entity;

import net.minecraft.entity.Entity;

public interface Infectable {
    boolean isInfected();

    void setInfected(boolean infected);

    default boolean canBeTargettedBy(Entity mob) {
        return !isInfected();
    }
}

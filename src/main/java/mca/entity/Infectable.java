package mca.entity;

import net.minecraft.entity.Entity;

public interface Infectable {
    boolean canBeTargettedBy(Entity mob);
}

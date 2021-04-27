package cobalt.minecraft.network.datasync;

import cobalt.minecraft.nbt.CNBT;

abstract public class CDataParameter {
    public abstract void register();

    public abstract void load(CNBT nbt);

    public abstract void save(CNBT nbt);
}

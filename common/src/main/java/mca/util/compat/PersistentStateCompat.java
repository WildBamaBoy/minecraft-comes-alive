package mca.util.compat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public abstract class PersistentStateCompat {
    private PersistentState outer;

    public final void attach(PersistentState outer) {
        this.outer = outer;
    }

    public abstract NbtCompound writeNbt(NbtCompound nbt);

    public void markDirty() {
        if (outer != null) {
            outer.markDirty();
        }
    }
}

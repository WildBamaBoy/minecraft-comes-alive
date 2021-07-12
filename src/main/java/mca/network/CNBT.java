package mca.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jetbrains.annotations.Nullable;

/**
 * Compressed NBT structure.
 */
public final class CNBT implements Serializable {
    private static final long serialVersionUID = 5728742776742369248L;

    transient private NbtCompound mcCompound;

    private CNBT(NbtCompound nbt) {
        mcCompound = nbt;
    }

    @Nullable
    public static CNBT wrap(@Nullable NbtCompound nbt) {
        if (nbt == null) {
            return null;
        } else {
            return new CNBT(nbt);
        }
    }

    public NbtCompound upwrap() {
        return mcCompound;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        NbtIo.write(mcCompound, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mcCompound = NbtIo.read(in);
    }
}

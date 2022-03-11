package mca.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import mca.cobalt.network.Message;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

public abstract class S2CNbtDataMessage implements Message {
    private static final long serialVersionUID = 3409849549326097419L;

    private final Data data;

    public S2CNbtDataMessage(NbtCompound data) {
        this.data = new Data(data);
    }

    public NbtCompound getData() {
        return data.nbt;
    }

    private static final class Data implements Serializable {
        private static final long serialVersionUID = 5728742776742369248L;

        transient NbtCompound nbt;

        Data(NbtCompound nbt) {
            this.nbt = nbt;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            NbtIo.write(nbt, out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            nbt = NbtIo.read(in);
        }
    }
}

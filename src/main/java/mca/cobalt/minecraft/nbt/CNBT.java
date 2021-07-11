package mca.cobalt.minecraft.nbt;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public class CNBT implements Serializable {
    private static final long serialVersionUID = 5728742776742369248L;

    transient private NbtCompound mcCompound;

    private CNBT() {
        this(new NbtCompound());
    }

    private CNBT(NbtCompound nbt) {
        mcCompound = nbt;
    }

    public static CNBT createNew() {
        return new CNBT();
    }

    public static CNBT fromMC(NbtCompound nbt) {
        if (nbt == null) {
            return null;
        } else {
            return new CNBT(nbt);
        }
    }

    public int getInteger(String key) {
        return mcCompound.getInt(key);
    }

    public String getString(String key) {
        return mcCompound.getString(key);
    }

    public float getFloat(String key) {
        return mcCompound.getFloat(key);
    }

    public double getDouble(String key) {
        return mcCompound.getDouble(key);
    }

    public boolean getBoolean(String key) {
        return mcCompound.getBoolean(key);
    }

    public UUID getUUID(String key) {
        if (mcCompound.containsUuid(key)) {
            return mcCompound.getUuid(key);
        } else {
            return Constants.ZERO_UUID;
        }
    }

    public byte getByte(String key) {
        return mcCompound.getByte(key);
    }

    public void setUUID(String key, UUID value) {
        mcCompound.putUuid(key, value);
    }

    public void setString(String key, String value) {
        mcCompound.putString(key, value);
    }

    public CNBT getCompoundTag(String key) {
        return CNBT.fromMC(mcCompound.getCompound(key));
    }

    public void setInteger(String key, int value) {
        mcCompound.putInt(key, value);
    }

    public CNBT setBoolean(String key, boolean value) {
        mcCompound.putBoolean(key, value);
        return this;
    }

    public CNBT setTag(String key, CNBT value) {
        mcCompound.put(key, value.mcCompound);
        return this;
    }

    public void setDouble(String key, double value) {
        mcCompound.putDouble(key, value);
    }

    public void setFloat(String key, float value) {
        mcCompound.putFloat(key, value);
    }

    public void setByte(String key, byte value) {
        mcCompound.putByte(key, value);
    }

    public void setList(String key, NbtList list) {
        mcCompound.put(key, list);
    }

    public CNBT copy() {
        return new CNBT(mcCompound.copy());
    }

    public NbtList getList(String key) {
        return mcCompound.getList(key, 9);
    }

    public NbtList getCompoundList(String key) {
        return mcCompound.getList(key, 10);
    }

    public BlockPos getBlockPos(String key) {
        int x, y, z;

        x = mcCompound.getInt(key + "X");
        y = mcCompound.getInt(key + "Y");
        z = mcCompound.getInt(key + "Z");

        return new BlockPos(x, y, z);
    }

    public void setBlockPos(String key, BlockPos pos) {
        this.setInteger(key + "X", pos.getX());
        this.setInteger(key + "Y", pos.getY());
        this.setInteger(key + "Z", pos.getZ());
    }

    public void set(String key, Object value) {
        Class<?> clazz = value.getClass();
        if (value instanceof Float) {
            setFloat(key, (Float) value);
        } else if (value instanceof Byte) {
            setByte(key, (Byte) value);
        } else if (value instanceof Double) {
            setDouble(key, (Double) value);
        } else if (value instanceof String) {
            setString(key, (String) value);
        } else if (value instanceof Integer) {
            setInteger(key, (Integer) value);
        } else if (value instanceof UUID) {
            setUUID(key, (UUID) value);
        } else if (value instanceof Boolean) {
            setBoolean(key, (Boolean) value);
        } else if (value instanceof CNBT) {
            setTag(key, (CNBT) value);
        } else {
            MCA.logger.throwing(new Exception("Attempt to set CNBT data of unknown class!: " + clazz.getName()));
        }
    }

    public Set<String> getKeySet() {
        return mcCompound.getKeys();
    }

    public NbtCompound getMcCompound() {
        return mcCompound;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        NbtIo.write(mcCompound, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mcCompound = NbtIo.read(in);
    }
}

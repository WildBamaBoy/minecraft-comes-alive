package mca.api.cobalt.minecraft.nbt;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public class CNBT implements Serializable {
    transient private CompoundNBT mcCompound;

    private CNBT() {
        mcCompound = new CompoundNBT();
    }

    private CNBT(CompoundNBT nbt) {
        mcCompound = nbt;
    }

    public static CNBT createNew() {
        return new CNBT();
    }

    public static CNBT fromMC(CompoundNBT nbt) {
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
        if (mcCompound.hasUUID(key)) {
            return mcCompound.getUUID(key);
        } else {
            return Constants.ZERO_UUID;
        }
    }

    public byte getByte(String key) {
        return mcCompound.getByte(key);
    }

    public void setUUID(String key, UUID value) {
        mcCompound.putUUID(key, value);
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

    public void setList(String key, ListNBT list) {
        mcCompound.put(key, list);
    }

    public CNBT copy() {
        return new CNBT(mcCompound.copy());
    }

    public ListNBT getList(String key) {
        return mcCompound.getList(key, 9);
    }

    public ListNBT getCompoundList(String key) {
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
            MCA.getMod().logger.throwing(new Exception("Attempt to set CNBT data of unknown class!: " + clazz.getName()));
        }
    }

    public Set<String> getKeySet() {
        return mcCompound.getAllKeys();
    }

    public CompoundNBT getMcCompound() {
        return mcCompound;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        CompressedStreamTools.write(mcCompound, out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        mcCompound = CompressedStreamTools.read(in);
    }
}

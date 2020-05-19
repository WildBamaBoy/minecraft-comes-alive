package mca.api.wrappers;

import java.util.UUID;

import lombok.Getter;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class NBTWrapper {
	@Getter private NBTTagCompound vanillaCompound;
	
	public NBTWrapper() {
		vanillaCompound = new NBTTagCompound();
	}
	
	public NBTWrapper(NBTTagCompound nbt) {
		vanillaCompound = nbt;
	}
	
	public int getInteger(String key) {
		return vanillaCompound.getInteger(key);
	}
	
	public String getString(String key) {
		return vanillaCompound.getString(key);
	}
	
	public float getFloat(String key) {
		return vanillaCompound.getFloat(key);
	}
	
	public double getDouble(String key) {
		return vanillaCompound.getDouble(key);
	}
	
	public boolean getBoolean(String key) {
		return vanillaCompound.getBoolean(key);
	}
	
	public UUID getUUID(String key) {
		return vanillaCompound.getUniqueId(key);
	}
	
	public void setUUID(String key, UUID value) {
		vanillaCompound.setUniqueId(key, value);
	}
	
	public void setString(String key, String value) {
		vanillaCompound.setString(key, value);
	}
	
	public NBTTagList getTagList(String key, int type) {
		return vanillaCompound.getTagList(key, type);
	}
	
	public NBTTagCompound getCompoundTag(String key) {
		return vanillaCompound.getCompoundTag(key);
	}

	public void setInteger(String key, int value) {
		vanillaCompound.setInteger(key, value);
	}

	public void setBoolean(String key, boolean value) {
		vanillaCompound.setBoolean(key, value);
	}

	public void setTag(String key, NBTBase value) {
		vanillaCompound.setTag(key, value);
	}

	public void setDouble(String key, double value) {
		vanillaCompound.setDouble(key, value);
	}

	public void setFloat(String key, float value) {
		vanillaCompound.setFloat(key, value);
	}
}

package mca.api.wrappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import mca.core.MCA;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.EntityDataManager.DataEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class DataManagerWrapper {
	@Getter private EntityDataManager vanillaManager;
	
	private HashMap<String, EntityDataManager.DataEntry<?>> registeredParams;
	
	public DataManagerWrapper(EntityDataManager vanillaManager) {
		this.vanillaManager = vanillaManager;
		this.registeredParams = new HashMap<>();
	}
	
	public <T> void register(DataParameter<T> key, T value, String name) {
		vanillaManager.register(key, value);
		registeredParams.put(name,  new EntityDataManager.DataEntry<T>(key, value));
	}

	public <T> void setDirty(DataParameter<T> key) {
		vanillaManager.setDirty(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void saveAll(NBTWrapper nbt) {
		for (Entry<String, DataEntry<?>> entry : registeredParams.entrySet()) {
			DataParameter<?> param = entry.getValue().getKey();
			DataSerializer<?> serializer = param.getSerializer();
			String paramName = entry.getKey();
			T paramValue = (T) vanillaManager.get(param);

			if (serializer == DataSerializers.VARINT) {
				nbt.setInteger(paramName, (Integer)paramValue);
			} else if (serializer == DataSerializers.FLOAT) {
				nbt.setFloat(paramName, (Float)paramValue);
			} else if (serializer == DataSerializers.STRING) {
				nbt.setString(paramName, (String)paramValue);
			} else if (serializer == DataSerializers.TEXT_COMPONENT) {
				nbt.setString(paramName, ((ITextComponent)paramValue).getFormattedText());
			} else if  (serializer == DataSerializers.ITEM_STACK) {
				nbt.setTag(paramName, ((ItemStack)paramValue).writeToNBT(new NBTTagCompound()));
			} else if (serializer == DataSerializers.BOOLEAN) {
				nbt.setBoolean(paramName, (Boolean)paramValue);
			} else if (serializer == DataSerializers.BLOCK_POS) {
				BlockPos pos = (BlockPos)paramValue;
				nbt.setInteger(paramName + "X", pos.getX());
				nbt.setInteger(paramName + "Y", pos.getY());
				nbt.setInteger(paramName + "Z", pos.getZ());
			} else if (serializer == DataSerializers.OPTIONAL_UNIQUE_ID) {
				// Odd mixing of java util's optionals and guava's optionals. Try to handle both.
				try {
					java.util.Optional uuid = (java.util.Optional)paramValue;
					nbt.setUUID(paramName, (UUID)uuid.orElse(new UUID(0,0)));
				} catch (ClassCastException e) {
					java.util.Optional uuid = (java.util.Optional)paramValue;
					nbt.setUUID(paramName, (UUID)uuid.orElse(new UUID(0,0)));
				}
			} else if (serializer == DataSerializers.COMPOUND_TAG) {
				nbt.setTag(paramName, (NBTTagCompound)paramValue);
			} else if (serializer == DataSerializers.BYTE) {
				nbt.setInteger(paramName, (Byte)paramValue);
			} else {
				MCA.getLog().error(String.format("Unrecognized value type `%s` on attempt to save param `%s`", paramValue.getClass().getName(), paramName));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> void loadAll(NBTWrapper nbt) {
		for (Entry<String, DataEntry<?>> entry : registeredParams.entrySet()) {
			DataParameter<?> param = entry.getValue().getKey();
			DataSerializer<?> serializer = param.getSerializer();
			String paramName = entry.getKey();

			if (serializer == DataSerializers.VARINT) {
				vanillaManager.set((DataParameter<T>)param, (T)Integer.valueOf(nbt.getInteger(paramName)));
			} else if (serializer == DataSerializers.FLOAT) {
				vanillaManager.set((DataParameter<T>)param, (T)Float.valueOf(nbt.getFloat(paramName)));
			} else if (serializer == DataSerializers.STRING) {
				vanillaManager.set((DataParameter<T>)param, (T)nbt.getString(paramName));
			} else if (serializer == DataSerializers.TEXT_COMPONENT) {
				vanillaManager.set((DataParameter<T>)param, (T)new TextComponentString(nbt.getString(paramName)));
			} else if  (serializer == DataSerializers.ITEM_STACK) {
				vanillaManager.set((DataParameter<T>)param, (T)new ItemStack(nbt.getCompoundTag(paramName)));
			} else if (serializer == DataSerializers.BOOLEAN) {
				vanillaManager.set((DataParameter<T>)param, (T)Boolean.valueOf(nbt.getBoolean(paramName)));
			} else if (serializer == DataSerializers.BLOCK_POS) {
				BlockPos pos = new BlockPos(nbt.getInteger(paramName + "X"), nbt.getInteger(paramName + "Y"), nbt.getInteger(paramName + "Z"));
				vanillaManager.set((DataParameter<T>)param, (T)pos);
			} else if (serializer == DataSerializers.OPTIONAL_UNIQUE_ID) {
				vanillaManager.set((DataParameter<T>)param, (T)java.util.Optional.of(nbt.getUUID(paramName)));
			} else if (serializer == DataSerializers.COMPOUND_TAG) {
				vanillaManager.set((DataParameter<T>)param, (T)nbt.getCompoundTag(paramName));
			} else if (serializer == DataSerializers.BYTE) {
				vanillaManager.set((DataParameter<T>)param, (T)Integer.valueOf(nbt.getInteger(paramName)));
			} else {
				MCA.getLog().error(String.format("Unrecognized serializer on attempt to read data param `%s`: %s", paramName, serializer.getClass().getName()));
			}
		}
	}
}

package radixcore.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;
import radixcore.packets.PacketWatchedUpdateC;
import radixcore.packets.PacketWatchedUpdateS;
import radixcore.util.RadixExcept;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public final class DataWatcherEx implements Serializable
{
	public static boolean allowClientSideModification = false;
	private static transient final HashMap dataTypes = new HashMap();

	private String initializingModId;
	private Map watchedObjects = new HashMap();
	private final Entity entityOwner;
	private Object objectOwner;
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	public DataWatcherEx(Object owner, String modId)
	{
		this.entityOwner = null;
		this.objectOwner = owner;
		this.initializingModId = modId;
	}

	public DataWatcherEx(Entity owner, String modId)
	{
		this.entityOwner = owner;
		this.objectOwner = null;
		this.initializingModId = modId;
	}

	public void addObject(int id, Object value)
	{
		Integer dataType = (Integer)dataTypes.get(value.getClass());

		if (dataType == null)
		{
			throw new IllegalArgumentException("Unknown data type: " + value.getClass());
		}

		else if (id > 255)
		{
			throw new IllegalArgumentException("Provided id value is too high! Maximum is 255.");
		}

		else if (this.watchedObjects.containsKey(Integer.valueOf(id)))
		{
			throw new IllegalArgumentException("Duplicate id value for " + id + "!");
		}

		else
		{
			WatchedObjectEx watchableobject = new WatchedObjectEx(dataType.intValue(), id, value);
			this.lock.writeLock().lock();
			this.watchedObjects.put(Integer.valueOf(id), watchableobject);
			this.lock.writeLock().unlock();
		}
	}

	public byte getWatchableObjectByte(int id)
	{
		return ((Byte)this.getWatchedObject(id).getObject()).byteValue();
	}

	public short getWatchableObjectShort(int id)
	{
		return ((Short)this.getWatchedObject(id).getObject()).shortValue();
	}

	public int getWatchableObjectInt(int id)
	{
		return ((Integer)this.getWatchedObject(id).getObject()).intValue();
	}

	public float getWatchableObjectFloat(int id)
	{
		return ((Float)this.getWatchedObject(id).getObject()).floatValue();
	}

	public String getWatchableObjectString(int id)
	{
		return (String)this.getWatchedObject(id).getObject();
	}

	public WatchedObjectEx getWatchedObject(int id)
	{
		this.lock.readLock().lock();
		WatchedObjectEx watchableobject = null;

		try
		{
			watchableobject = (WatchedObjectEx)this.watchedObjects.get(Integer.valueOf(id));
		}

		catch (Throwable throwable)
		{
			RadixExcept.logFatalCatch(throwable, "Error getting watched object data.");
		}

		this.lock.readLock().unlock();
		return watchableobject;
	}

	public Map getWatchedDataMap() 
	{
		return watchedObjects;
	}

	public void updateObject(int id, Object newValue, boolean dispatch)
	{
		WatchedObjectEx watchableObject = this.getWatchedObject(id);
		watchableObject.setObject(newValue);

		if ((entityOwner != null || objectOwner != null) && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			PacketWatchedUpdateC packet = null;
			TargetPoint point = null; 
			
			if (entityOwner != null)
			{
				packet = new PacketWatchedUpdateC(entityOwner.getEntityId(), id, newValue);
				point = new TargetPoint(entityOwner.dimension, entityOwner.posX, entityOwner.posY, entityOwner.posZ, 50);
			}

			else
			{
				packet = new PacketWatchedUpdateC(initializingModId, id, newValue);
			}
			
			if (point == null)
			{
				RadixCore.getPacketHandler().sendPacketToAllPlayers(packet);
			}
			
			else
			{
				RadixCore.getPacketHandler().sendPacketToAllAround(packet, point);
			}
			
			if (objectOwner instanceof AbstractPlayerData)
			{
				for (ModMetadataEx metadata : RadixCore.getRegisteredMods())
				{
					if (metadata.modId.equals(initializingModId))
					{
						AbstractPlayerData data = (AbstractPlayerData) objectOwner;
						
						if (data.owner == null)
						{
							metadata.playerDataMap.put(data.ownerIdentifier, data);
						}
						
						else
						{
							metadata.playerDataMap.put(data.owner.getUniqueID().toString(), data);
						}
					}
				}
			}
		}

		else if (dispatch)
		{
			if (allowClientSideModification)
			{
				PacketWatchedUpdateS packet = null;

				if (entityOwner != null)
				{
					packet = new PacketWatchedUpdateS(entityOwner.getEntityId(), id, newValue);
				}

				else
				{
					packet = new PacketWatchedUpdateS(initializingModId, id, newValue);	
				}

				RadixCore.getPacketHandler().sendPacketToServer(packet);
			}

			else
			{
				RadixExcept.logErrorCatch(new Throwable(), "Attempted to modify watched value client-side without qualifying that this was intended!");
			}
		}
	}

	public void writeDataWatcherToNBT(NBTTagCompound nbt)
	{
		try
		{
			for (Object object : watchedObjects.values())
			{
				WatchedObjectEx watchableObject = (WatchedObjectEx)object;

				switch (watchableObject.getObjectType())
				{
				case Byte:
					nbt.setByte("dataWatcherExObject" + watchableObject.getDataValueId(), (Byte)watchableObject.getObject());
					break;
				case Float:
					nbt.setFloat("dataWatcherExObject" + watchableObject.getDataValueId(), (Float)watchableObject.getObject());
					break;
				case Integer:
					nbt.setInteger("dataWatcherExObject" + watchableObject.getDataValueId(), (Integer)watchableObject.getObject());
					break;
				case Short:
					nbt.setShort("dataWatcherExObject" + watchableObject.getDataValueId(), (Short)watchableObject.getObject());
					break;
				case String:
					nbt.setString("dataWatcherExObject" + watchableObject.getDataValueId(), (String)watchableObject.getObject());
					break;
				default:
					RadixExcept.logErrorCatch(new Throwable(), "Error writing watched object " + watchableObject.getDataValueId() + " to NBT. Invalid data type.");
					continue;
				}
			}
		}

		catch (Exception e)
		{
			RadixExcept.logErrorCatch(e, "Error saving data watcher to NBT.");
		}
	}

	public void readDataWatcherFromNBT(NBTTagCompound nbt)
	{
		try
		{
			for (Object object : watchedObjects.values())
			{
				WatchedObjectEx watchableObject = (WatchedObjectEx)object;

				switch (watchableObject.getObjectType())
				{
				case Byte:
					watchableObject.setObject(nbt.getByte("dataWatcherExObject" + watchableObject.getDataValueId()));
					break;
				case Float:
					watchableObject.setObject(nbt.getFloat("dataWatcherExObject" + watchableObject.getDataValueId()));
					break;
				case Integer:
					watchableObject.setObject(nbt.getInteger("dataWatcherExObject" + watchableObject.getDataValueId()));
					break;
				case Short:
					watchableObject.setObject(nbt.getShort("dataWatcherExObject" + watchableObject.getDataValueId()));
					break;
				case String:
					watchableObject.setObject(nbt.getString("dataWatcherExObject" + watchableObject.getDataValueId()));
					break;
				default:
					RadixExcept.logErrorCatch(new Throwable(), "Error reading watched object " + watchableObject.getDataValueId() + " from NBT. Invalid data type.");
					continue;
				}
			}
		}

		catch (Exception e)
		{
			RadixExcept.logErrorCatch(e, "Error saving data watcher to NBT.");
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}

	public void setObjectOwner(Object obj)
	{
		this.objectOwner = obj;
	}

	static
	{
		dataTypes.put(Byte.class, Integer.valueOf(0));
		dataTypes.put(Short.class, Integer.valueOf(1));
		dataTypes.put(Integer.class, Integer.valueOf(2));
		dataTypes.put(Float.class, Integer.valueOf(3));
		dataTypes.put(String.class, Integer.valueOf(4));
	}
}
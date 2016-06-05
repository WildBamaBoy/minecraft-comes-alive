package mca.data;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import cpw.mods.fml.common.FMLCommonHandler;
import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/**
 * This class saves/loads villager data for those that have died and are related to players.
 * 
 * NBT structure is as follows:
 * 	 mca-revivable-villagers.dat
 *	  |--->data
 *		    |--->VillagerData-[uuid]
 *		           |--->relevant villager data from VillagerSaveData object.
 */
public final class RevivableVillagerManager extends WorldSavedData
{
	private static final String ID = "mca-revivable-villagers";
	
	private static final Map<UUID, VillagerSaveData> villagerData = new TreeMap<UUID, VillagerSaveData>();

	/** Required by Forge */
	public RevivableVillagerManager() 
	{
		this(ID);
	}

	/** Required by Forge */
	public RevivableVillagerManager(String id) 
	{
		super(id);
	}

	public static RevivableVillagerManager get() 
	{
		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
		RevivableVillagerManager manager = (RevivableVillagerManager)world.loadItemData(RevivableVillagerManager.class, ID);
	
		if (manager == null)
		{
			manager = new RevivableVillagerManager();
			world.setItemData(ID, manager);
		}
	
		return manager;
	}
	
	public void addVillagerData(EntityHuman human, UUID ownerUUID)
	{
		villagerData.put(human.getUniqueID(), VillagerSaveData.fromVillager(human, null, ownerUUID));
		markDirty();
	}
	
	public void removeVillagerData(UUID uuid)
	{
		villagerData.remove(uuid);
		markDirty();
	}
	
	public VillagerSaveData getVillagerSaveDataByUUID(UUID uuid)
	{
		return villagerData.get(uuid);
	}
	
	public void clearVillagerData()
	{
		villagerData.clear();
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		for (Map.Entry<UUID, VillagerSaveData> entry : villagerData.entrySet())
		{
			NBTTagCompound deadVillagerData = new NBTTagCompound();

			UUID uuid = entry.getKey();
			VillagerSaveData data = entry.getValue();
						
			data.writeDataToNBT(deadVillagerData);
			
			nbt.setTag("VillagerData-" + uuid.toString(), deadVillagerData);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		for (Object nbtKey : nbt.func_150296_c()) //Loop through all keys.
		{
			String keyString = (String)nbtKey;
			
			NBTTagCompound villagerDataCollection = nbt.getCompoundTag(keyString);
			VillagerSaveData data = VillagerSaveData.fromNBT(villagerDataCollection);
			
			villagerData.put(data.uuid, data);
		}
		
		markDirty();
	}
}

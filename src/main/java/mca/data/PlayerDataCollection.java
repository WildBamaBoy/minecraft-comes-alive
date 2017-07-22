package mca.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mca.core.MCA;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayerDataCollection extends WorldSavedData 
{	
	private static final String ID = "MinecraftComesAlive";

	/** Map of player UUIDs and the list of quests they've encountered. */
	private Map<UUID, NBTPlayerData> playerDataMap;

	public static PlayerDataCollection get() 
	{
		World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
		PlayerDataCollection data = (PlayerDataCollection) world.loadData(PlayerDataCollection.class, ID);

		if (data == null)
		{
			data = new PlayerDataCollection();
			world.setData(ID, data);
		}

		return data;
	}

	public PlayerDataCollection() 
	{
		this(ID);
	}

	public PlayerDataCollection(String id) 
	{
		super(id);
		playerDataMap = new HashMap<UUID, NBTPlayerData>();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		for (Map.Entry<UUID, NBTPlayerData> entry : playerDataMap.entrySet())
		{
			UUID uuid = entry.getKey();
			NBTPlayerData playerData = entry.getValue();
			NBTTagCompound playerDataTags = new NBTTagCompound();
			
			try 
			{
				playerData.writeToNBT(playerDataTags);
				nbt.setTag("PlayerData-" + uuid.toString(), playerDataTags);
			}

			catch (Exception e)
			{
				MCA.getLog().error("Error writing player data to NBT for UUID " + uuid.toString() + ". Progress may be corrupted or lost.");
				continue;
			}
		}
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		for (Object tagKey : nbt.getKeySet()) //Loop through all keys.
		{
			try
			{
				String tagName = (String)tagKey;
				UUID uuid = UUID.fromString(tagName.replace("PlayerData-", ""));
				NBTTagCompound playerDataTags = nbt.getCompoundTag(tagName);
				NBTPlayerData data = new NBTPlayerData();

				data.readFromNBT(playerDataTags);
				playerDataMap.put(uuid, data);
			}

			catch (Exception e)
			{
				MCA.getLog().error("Error reading player data from NBT tag: " + (String)tagKey + ". Progress may be corrupted or lost.");
				continue;
			}
		}

		markDirty();
	}

	public NBTPlayerData getPlayerData(UUID uuid)
	{
		return playerDataMap.get(uuid);
	}

	public void putPlayerData(UUID uuid, NBTPlayerData data)
	{
		playerDataMap.put(uuid, data);
		markDirty();
	}

	public UUID getUUIDByReference(NBTPlayerData data)
	{
		for (Map.Entry<UUID, NBTPlayerData> entry : playerDataMap.entrySet())
		{
			if (entry.getValue() == data)
			{
				return entry.getKey();
			}
		}
		
		return null;
	}
}

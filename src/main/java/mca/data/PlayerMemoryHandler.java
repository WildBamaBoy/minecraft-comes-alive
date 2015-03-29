package mca.data;

import java.util.Map;

import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;

public final class PlayerMemoryHandler 
{
	public static void readPlayerMemoryFromNBT(EntityHuman human, Map<String, PlayerMemory> playerMemories, NBTTagCompound nbt)
	{
		int counter = 0;
		
		while (true)
		{
			final String playerName = nbt.getString("playerMemoryKey" + counter);

			if (playerName.equals(""))
			{
				break;
			}

			else
			{
				final PlayerMemory playerMemory = new PlayerMemory(human, playerName);
				playerMemory.readPlayerMemoryFromNBT(nbt);
				playerMemories.put(playerName, playerMemory);
				counter++;
			}
		}
	}
	
	public static void writePlayerMemoryToNBT(Map<String, PlayerMemory> playerMemories, NBTTagCompound nbt)
	{
		int counter = 0;
		
		for (Map.Entry<String, PlayerMemory> keyValuePair : playerMemories.entrySet())
		{
			nbt.setString("playerMemoryKey" + counter, keyValuePair.getKey());
			keyValuePair.getValue().writePlayerMemoryToNBT(nbt);
			counter++;
		}
	}
	
	private PlayerMemoryHandler() {}
}

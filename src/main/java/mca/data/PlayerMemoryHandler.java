//package mca.data;
//
//import java.util.Map;
//import java.util.UUID;
//
//import mca.entity.EntityVillagerMCA;
//import net.minecraft.nbt.NBTTagCompound;
//
//@Deprecated
//public final class PlayerMemoryHandler 
//{
//	public static void readPlayerMemoryFromNBT(EntityVillagerMCA human, Map<String, PlayerMemory> playerMemories, NBTTagCompound nbt)
//	{
//		int counter = 0;
//		
//		while (true)
//		{
//			final String playerName = nbt.getString("playerMemoryKey" + counter);
//
//			if (playerName.equals(""))
//			{
//				break;
//			}
//
//			else
//			{
//				final PlayerMemory playerMemory = new PlayerMemory(human, playerName);
//				playerMemory.readPlayerMemoryFromNBT(nbt);
//				playerMemories.put(playerName, playerMemory);
//				counter++;
//			}
//		}
//	}
//	
//	public static void writePlayerMemoryToNBT(Map<UUID, PlayerMemory> playerMemories, NBTTagCompound nbt)
//	{
//		int counter = 0;
//		
//		for (Map.Entry<UUID, PlayerMemory> keyValuePair : playerMemories.entrySet())
//		{
//			nbt.setUniqueId("playerMemoryKey" + counter, keyValuePair.getKey());
//			keyValuePair.getValue().writePlayerMemoryToNBT(nbt);
//			counter++;
//		}
//	}
//	
//	private PlayerMemoryHandler() {}
//}

package mca.api;

import java.util.HashMap;
import java.util.Map;

import mca.api.exception.MappingNotFoundException;
import net.minecraft.block.Block;
import radixcore.data.BlockObj;

public final class ChoreRegistry 
{
	private static final Map<Object, Integer> giftMap = new HashMap<Object, Integer>();
	private static final Map<Integer, Block> notifyBlockMap = new HashMap<Integer, Block>();
	private static final Map<Integer, WoodcuttingEntry> woodcuttingBlockMap = new HashMap<Integer, WoodcuttingEntry>();
	
	public static Integer getIdOfNotifyBlock(Block block)
	{
		return reverseLookupKey(notifyBlockMap, block);
	}
	
	public static Block getNotifyBlockById(int id) throws MappingNotFoundException
	{
		Block block = notifyBlockMap.get(id);
		
		if (block != null)
		{
			return block;
		}
		
		else
		{
			throw new MappingNotFoundException();
		}
	}

	public static WoodcuttingEntry getWoodcuttingEntryById(int id) throws MappingNotFoundException
	{
		WoodcuttingEntry entry = woodcuttingBlockMap.get(id);
		
		if (entry != null)
		{
			return entry;
		}
		
		else
		{
			throw new MappingNotFoundException();
		}
	}
	
	public static void addObjectAsGift(Object blockOrItem, int giftValue)
	{
		giftMap.put(blockOrItem, giftValue);
	}
	
	public static void addBlockToMiningAI(int id, Block block)
	{
		putIfNotDuplicate(notifyBlockMap, id, block);
	}

	public static void addBlockToWoodcuttingAI(int id, WoodcuttingEntry entry)
	{
		putIfNotDuplicate(woodcuttingBlockMap, id, entry);
	}
	
	public static Map<Object, Integer> getGiftMap()
	{
		return giftMap;
	}
	
	private ChoreRegistry()
	{
	}

	private static <K, V> void putIfNotDuplicate(Map<K, V> map, K key, V value)
	{
		if (map.containsKey(key))
		{
			throw new IllegalArgumentException("Key is already contained in map for key/value pair: key = " + key + ", value = " + value);
		}
		
		else if (map.containsValue(value))
		{
			throw new IllegalArgumentException("Value is already contained in map for key/value pair: key = " + key + ", value = " + value);
		}
		
		else
		{
			map.put(key, value);
		}
	}
	
	private static <K, V> K reverseLookupKey(Map<K, V> map, V value)
	{
		for (Map.Entry<K, V> entry : map.entrySet())
		{
			if (entry.getValue() == value)
			{
				return entry.getKey();
			}
		}
		
		return null;
	}
}

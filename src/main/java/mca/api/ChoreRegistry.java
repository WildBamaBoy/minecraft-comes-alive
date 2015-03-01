package mca.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import radixcore.data.BlockObj;

public final class ChoreRegistry 
{
	private static final Map<Object, Integer> giftMap = new HashMap<Object, Integer>();
	private static final Map<Integer, Block> notifyBlockMap = new HashMap<Integer, Block>();
	private static final Map<Integer, BlockObj> woodcuttingBlockMap = new HashMap<Integer, BlockObj>();
	
	public static Integer getIdOfNotifyBlock(Block block)
	{
		return reverseLookupKey(notifyBlockMap, block);
	}
	
	public static Integer getIdOfWoodcuttingBlock(BlockObj block)
	{
		for (Map.Entry<Integer, BlockObj> entry : woodcuttingBlockMap.entrySet())
		{
			BlockObj value = entry.getValue();
			
			if (value.getBlock() == block.getBlock() && value.getMeta() == block.getMeta())
			{
				return entry.getKey();
			}
		}
		
		return 0;
	}
	
	public static Block getNotifyBlockById(int id)
	{
		return notifyBlockMap.get(id);
	}

	public static BlockObj getWoodcuttingBlockById(int id)
	{
		return woodcuttingBlockMap.get(id);
	}
	
	public static void addObjectAsGift(Object blockOrItem, int giftValue)
	{
		giftMap.put(blockOrItem, giftValue);
	}
	
	public static void addBlockToMiningAI(int id, Block block)
	{
		putIfNotDuplicate(notifyBlockMap, id, block);
	}

	public static void addBlockToWoodcuttingAI(int id, Block block, int meta)
	{
		putIfNotDuplicate(woodcuttingBlockMap, id, new BlockObj(block, meta));
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

package mca.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;

public final class APICoreMCA 
{
	private static final Map<Object, Integer> giftMap = new HashMap<Object, Integer>();
	private static final Map<Block, Integer> notifyBlockMap = new HashMap<Block, Integer>();
	
	/**
	 * 
	 * 
	 * @param block	The block to search for while mining.
	 * @param id	A unique id for the block you are adding. MCA reserves 1 - 8.
	 */
	public static void addBlockToMiningAI(Block block, int id)
	{
		if (notifyBlockMap.containsKey(block))
		{
			throw new IllegalArgumentException("Block for mining AI is already registered: " + block);
		}
		
		else if (notifyBlockMap.containsValue(id))
		{
			throw new IllegalArgumentException("ID for mining AI is already registered: " + id);
		}
		
		else
		{
			notifyBlockMap.put(block, id);
		}
	}
	
	public static int getIdOfNotifyBlock(Block block)
	{
		return notifyBlockMap.get(block);
	}
	
	public static Block getNotifyBlockById(int id)
	{
		for (Map.Entry<Block, Integer> entry : notifyBlockMap.entrySet())
		{
			if (entry.getValue() == id)
			{
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	public static void addObjectAsGift(Object blockOrItem, int giftValue)
	{
		giftMap.put(blockOrItem, giftValue);
	}
	
	public static Map<Object, Integer> getGiftMap()
	{
		return giftMap;
	}
}

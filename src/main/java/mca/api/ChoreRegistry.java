package mca.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mca.api.exception.MappingNotFoundException;
import net.minecraft.block.Block;

public final class ChoreRegistry 
{
	private static final Map<Object, Integer> giftMap = new HashMap<Object, Integer>();
	private static final Map<Integer, Block> notifyBlockMap = new HashMap<Integer, Block>();
	private static final Map<Integer, WoodcuttingEntry> woodcuttingBlockMap = new HashMap<Integer, WoodcuttingEntry>();
	private static final List<Class> huntingKillableEntities = new ArrayList<Class>();
	private static final List<Class> huntingTameableEntities = new ArrayList<Class>();
	private static final List<CookableFood> cookableFood = new ArrayList<CookableFood>();
	
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
	
	/**
	 * Adds an entity that will appear during the hunting AI. <p>
	 * 
	 * This will require a <b>fully functional constructor that accepts only a World object as its argument</b>. <p>
	 * 
	 * This adds the entity to <b>BOTH</b> the killable and tameable list.
	 * 
	 * @param 	clazz	The class of the entity to add to the hunting AI.
	 */
	public static void addEntityToHuntingAI(Class clazz)
	{
		addEntityToHuntingAI(clazz, true);
		addEntityToHuntingAI(clazz, false);
	}

	/**
	 * Adds an entity that will appear during the hunting AI. <p>
	 * 
	 * This will require a <b>fully functional constructor that accepts only a World object as its argument</b>.
	 * 
	 * @param 	clazz		The class of the entity to add to the hunting AI.
	 * @param	isKillable	Whether or not this entity is killable. False will mean this entity will only appear during taming.
	 */
	public static void addEntityToHuntingAI(Class clazz, boolean isKillable)
	{
		if (isKillable)
		{
			huntingKillableEntities.add(clazz);
		}
		
		else
		{
			huntingTameableEntities.add(clazz);
		}
	}
	
	public static Class getRandomHuntingEntity(boolean shouldBeTameable)
	{
		if (shouldBeTameable)
		{
			int index = new Random().nextInt(huntingTameableEntities.size());
			return huntingTameableEntities.get(index);
		}
		
		else
		{
			int index = new Random().nextInt(huntingKillableEntities.size());
			return huntingKillableEntities.get(index);
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
	
	public static void addFoodToCookingAI(CookableFood foodObj)
	{
		cookableFood.add(foodObj);
	}
	
	public static Map<Object, Integer> getGiftMap()
	{
		return giftMap;
	}
	
	public static List<CookableFood> getCookableFoodList()
	{
		return cookableFood;
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

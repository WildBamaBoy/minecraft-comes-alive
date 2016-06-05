/*******************************************************************************
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mca.api.enums.EnumGiftCategory;
import mca.api.exception.MappingNotFoundException;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import radixcore.util.RadixMath;

/**
 * <p>The main registry used by MCA to manage what items will be used 
 * with chores.
 * 
 * <p>All methods prefixed with <code>add</code> will allow you to 
 * expand a particular chore with the provided arguments.
 * 
 * <p>All methods prefixed with <code>get</code> will return 
 * unmodifiable lists containing the objects already registered with a particular chore.
 */
public final class RegistryMCA 
{
	public static final Random rand = new Random();

	private static final Map<Object, Integer> giftMap = new HashMap<Object, Integer>();
	private static final Map<Integer, MiningEntry> miningEntryMap = new HashMap<Integer, MiningEntry>();
	private static final Map<Integer, WoodcuttingEntry> woodcuttingBlockMap = new HashMap<Integer, WoodcuttingEntry>();
	private static final Map<Integer, CropEntry> cropEntryMap = new HashMap<Integer, CropEntry>();
	private static final Map<Integer, FishingEntry> fishingEntryMap = new HashMap<Integer, FishingEntry>();
	private static final List<Class> huntingKillableEntities = new ArrayList<Class>();
	private static final List<Class> huntingTameableEntities = new ArrayList<Class>();
	private static final List<CookableFood> cookableFood = new ArrayList<CookableFood>();
	private static final List<WeddingGift> villagerGiftsBad = new ArrayList<WeddingGift>();
	private static final List<WeddingGift> villagerGiftsGood = new ArrayList<WeddingGift>();
	private static final List<WeddingGift> villagerGiftsBetter = new ArrayList<WeddingGift>();
	private static final List<WeddingGift> villagerGiftsBest = new ArrayList<WeddingGift>();

	/**
	 * Adds the provided object to the map of gifts and gift values.
	 * 
	 * @param 	blockOrItem		An instance of an item or a block that will
	 * 							be recognized as a gift. An exception will be 
	 * 							thrown if the object is not a block or item.
	 * 
	 * @param 	giftValue		The relationship value of a particular gift.
	 * 							10 value points equals one villager "heart".
	 */
	public static void addObjectAsGift(Object blockOrItem, int giftValue)
	{
		if (blockOrItem instanceof Block || blockOrItem instanceof Item)
		{
			giftMap.put(blockOrItem, giftValue);
		}

		else
		{
			throw new IllegalArgumentException("Provided gift object was not a block or an item.");
		}
	}

	/**
	 * Adds the provided fishing entry to the fishing AI if it is
	 * not already added.
	 * 
	 * Throws an exception if the entry or ID is already registered.
	 * 
	 * @param 	id		A unique ID for the FishingEntry.
	 * @param 	entry 	A unique FishingEntry.
	 */
	public static void addFishingEntryToFishingAI(int id, FishingEntry entry)
	{
		putIfNotDuplicate(fishingEntryMap, id, entry);
	}
	
	/**
	 * Adds the provided crop entry to the farming AI if it is
	 * not already added.
	 * 
	 * Throws an exception if the entry or ID is already registered.
	 * 
	 * @param 	id		A unique ID for the CropEntry.
	 * @param 	entry 	A unique CropEntry.
	 */
	public static void addCropToFarmingAI(int id, CropEntry entry)
	{
		putIfNotDuplicate(cropEntryMap, id, entry);
	}

	/**
	 * Adds the provided woodcutting entry to the woodcutting AI if
	 * it is not already added.
	 * 
	 * Throws an exception if the entry or ID is already registered.
	 * 
	 * @param id	A unique ID for the WoodcuttingEntry.
	 * @param entry	A unique WoodcuttingEntry.
	 */
	public static void addBlockToWoodcuttingAI(int id, WoodcuttingEntry entry)
	{
		putIfNotDuplicate(woodcuttingBlockMap, id, entry);
	}

	/**
	 * Adds the provided block to the mining AI if
	 * it is not already added.
	 * 
	 * Throws an exception if the block or ID is already registered.
	 * 
	 * @param id	A unique ID for the block.
	 * @param entry	A unique MiningEntry to add to the mining AI.
	 */
	public static void addBlockToMiningAI(int id, MiningEntry entry)
	{
		putIfNotDuplicate(miningEntryMap, id, entry);
	}

	/**
	 * <p>Adds an entity that will appear during the hunting AI.
	 * 
	 * <p>This will require a <b>fully functional constructor</b> that 
	 * accepts only a <code>World</code> object as its argument.
	 * 
	 * <p>This overloaded method adds the entity to <u>both</u> the killable 
	 * and tameable list.
	 * 
	 * @param 	clazz	The class of the entity to add to the hunting AI.
	 */
	public static void addEntityToHuntingAI(Class clazz)
	{
		addEntityToHuntingAI(clazz, true);
		addEntityToHuntingAI(clazz, false);
	}

	/**
	 * @see 	RegistryMCA#addEntityToHuntingAI(Class)
	 * 
	 * @param	isKillable	Whether or not this entity is killable. 
	 * 						False will mean this entity will only appear 
	 * 						during taming.
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

	/**
	 * Adds a CookableFood object to the cooking AI.
	 * 
	 * @param 	foodObj	A CookableFood object.
	 * @see CookableFood
	 */
	public static void addFoodToCookingAI(CookableFood foodObj)
	{
		cookableFood.add(foodObj);
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

	public static MiningEntry getMiningEntryById(int id) throws MappingNotFoundException
	{
		MiningEntry entry = miningEntryMap.get(id);

		if (entry != null)
		{
			return entry;
		}

		else
		{
			throw new MappingNotFoundException();
		}
	}

	public static Integer getIdOfMiningEntryContainingBlock(Block block) throws MappingNotFoundException
	{
		for (Map.Entry<Integer, MiningEntry> entry : miningEntryMap.entrySet())
		{
			MiningEntry theEntry = entry.getValue();

			if (theEntry.getBlock().equals(block))
			{
				return entry.getKey();
			}
		}

		throw new MappingNotFoundException();
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

	public static WoodcuttingEntry getDefaultWoodcuttingEntry()
	{
		return woodcuttingBlockMap.get(1);
	}

	public static CropEntry getDefaultCropEntry()
	{
		return cropEntryMap.get(1);
	}

	public static Map<Object, Integer> getGiftMap()
	{
		return Collections.unmodifiableMap(giftMap);
	}

	public static List<CookableFood> getCookableFoodList()
	{
		return Collections.unmodifiableList(cookableFood);
	}

	public static Map<Integer, MiningEntry> getMiningEntryMap()
	{
		return Collections.unmodifiableMap(miningEntryMap);
	}
	
	public static List<Integer> getWoodcuttingBlockIDs()
	{
		List<Integer> returnList = new ArrayList<Integer>();

		for (Map.Entry<Integer, WoodcuttingEntry> entry : woodcuttingBlockMap.entrySet())
		{
			returnList.add(entry.getKey());
		}

		return Collections.unmodifiableList(returnList);
	}

	public static List<Integer> getMiningEntryIDs()
	{
		List<Integer> returnList = new ArrayList<Integer>();

		for (Map.Entry<Integer, MiningEntry> entry : miningEntryMap.entrySet())
		{
			returnList.add(entry.getKey());
		}

		return Collections.unmodifiableList(returnList);
	}

	public static CropEntry getCropEntryById(int id) throws MappingNotFoundException
	{
		final CropEntry entry = cropEntryMap.get(id);	

		if (entry != null)
		{
			return entry;
		}

		else
		{
			throw new MappingNotFoundException();
		}
	}

	public static List<Integer> getCropEntryIDs() 
	{
		List<Integer> returnList = new ArrayList<Integer>();

		for (Map.Entry<Integer, CropEntry> entry : cropEntryMap.entrySet())
		{
			returnList.add(entry.getKey());
		}

		return returnList;
	}

	public static FishingEntry getRandomFishingEntry()
	{
		return fishingEntryMap.get(RadixMath.getNumberInRange(0, fishingEntryMap.size() - 1));
	}
	
	public static FishingEntry getFishingEntryById(int id) throws MappingNotFoundException
	{
		final FishingEntry entry = fishingEntryMap.get(id);	

		if (entry != null)
		{
			return entry;
		}

		else
		{
			throw new MappingNotFoundException();
		}
	}

	public static List<Integer> getFishingEntryIDs() 
	{
		List<Integer> returnList = new ArrayList<Integer>();

		for (Map.Entry<Integer, CropEntry> entry : cropEntryMap.entrySet())
		{
			returnList.add(entry.getKey());
		}

		return returnList;
	}
	
	public static void addWeddingGift(WeddingGift gift, EnumGiftCategory category)
	{
		switch (category)
		{
		case BAD: villagerGiftsBad.add(gift); break;
		case BEST: villagerGiftsBest.add(gift); break;
		case BETTER: villagerGiftsBetter.add(gift); break;
		case GOOD: villagerGiftsGood.add(gift); break;
		default:
			break;
		}
	}

	public static ItemStack getGiftStackFromRelationship(int heartsLevel)
	{
		List<WeddingGift> giftList = null;

		if (heartsLevel < 0)
		{
			giftList = villagerGiftsBad;
		}

		else if (heartsLevel >= 0 && heartsLevel <= 25)
		{
			giftList = villagerGiftsGood;
		}

		else if (heartsLevel > 25 && heartsLevel <= 50)
		{
			giftList = villagerGiftsBetter;
		}

		else
		{
			giftList = villagerGiftsBest;
		}

		Random rand = new Random();
		WeddingGift giftEntry = giftList.get(rand.nextInt(giftList.size()));
		return new ItemStack(giftEntry.getItem(), rand.nextInt(giftEntry.getMaximum() - giftEntry.getMinimum() + 1) + giftEntry.getMinimum());
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

	private RegistryMCA()
	{
	}
}

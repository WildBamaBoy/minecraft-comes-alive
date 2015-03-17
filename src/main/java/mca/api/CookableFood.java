package mca.api;

import net.minecraft.item.Item;

/**
 * Defines a food that can be cooked by identifying the item that 
 * will be used as the "raw" food, and the item that the raw food 
 * will be exchanged for when cooked.
 */
public final class CookableFood 
{
	private final Item foodRaw;
	private final Item foodCooked;
	
	/**
	 * Constructs a new CookableFood object from a raw food item
	 * and its cooked food item.
	 */
	public CookableFood(Item foodRaw, Item foodCooked)
	{
		this.foodRaw = foodRaw;
		this.foodCooked = foodCooked;
	}
	
	public Item getFoodRaw()
	{
		return foodRaw;
	}

	public Item getFoodCooked()
	{
		return foodCooked;
	}
}

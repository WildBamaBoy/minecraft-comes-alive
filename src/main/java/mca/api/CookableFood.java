package mca.api;

import net.minecraft.item.Item;

public final class CookableFood 
{
	private final Item foodRaw;
	private final Item foodCooked;
	
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

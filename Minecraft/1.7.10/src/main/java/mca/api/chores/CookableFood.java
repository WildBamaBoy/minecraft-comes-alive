/*******************************************************************************
 * CookableFood.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import net.minecraft.item.Item;

/**
 * Food that can be cooked by an MCA villager.
 */
public class CookableFood
{
	private final Item itemFoodRaw;
	private final Item itemFoodCooked;

	/**
	 * Constructs a cookable food entry. This is the only constuctor.
	 * <p>
	 * MCA adds porkchops, for example, as such:
	 * <p>
	 * <code>
	 * ChoreRegistry.registerChoreEntry(new CookableFood(Items.porkchop, Items.cooked_porkchop));
	 * </code>
	 * 
	 * @param itemFoodRaw The raw food item.
	 * @param itemFoodCooked The food item returned after being cooked.
	 */
	public CookableFood(Item itemFoodRaw, Item itemFoodCooked)
	{
		this.itemFoodRaw = itemFoodRaw;
		this.itemFoodCooked = itemFoodCooked;
	}

	public Item getRawFoodItem()
	{
		return itemFoodRaw;
	}

	public Item getCookedFoodItem()
	{
		return itemFoodCooked;
	}
}

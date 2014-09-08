/*******************************************************************************
 * CatchableFish.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import net.minecraft.item.Item;

/**
 * A fish that can be caught by an MCA villager.
 */
public class CatchableFish
{
	private final Item itemFish;
	private final int itemDamage;

	/**
	 * Constructs a catchable fish with an item damage of 0. Use this if your fish item doesn't have any subitems.
	 * <p>
	 * MCA adds the original Minecraft fish with this. Ex:
	 * <p>
	 * <code>
	 * ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish));
	 * </code>
	 * 
	 * @param itemFish The item that will be added to the fishing person's inventory when they successfully catch it.
	 */
	public CatchableFish(Item itemFish)
	{
		this.itemFish = itemFish;
		itemDamage = 0;
	}

	/**
	 * Constructs a catchable fish with a variable item damage. Use this if your fish item has subitems you'd like to add.
	 * <p>
	 * MCA uses this to add the pufferfish, salmon, etc. to the list of catchable fish. Ex:
	 * <p>
	 * <code>
	 * ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish, ItemFishFood.FishType.SALMON.func_150976_a()));
	 * </code>
	 * 
	 * @param itemFish The item that will be added to the fishing person's inventory when they successfully catch it.
	 * @param itemDamage The "damage" or metadata that the item should have.
	 */
	public CatchableFish(Item itemFish, int itemDamage)
	{
		this.itemFish = itemFish;
		this.itemDamage = itemDamage;
	}

	public Item getFishItem()
	{
		return itemFish;
	}

	public int getItemDamage()
	{
		return itemDamage;
	}
}

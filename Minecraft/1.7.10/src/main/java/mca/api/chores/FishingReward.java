/*******************************************************************************
 * FishingReward.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * A random find added to a fisher's inventory based on their experience.
 */
public class FishingReward
{
	private final boolean isEnhanced;
	private final int minimumReturn;
	private final int maximumReturn;
	private final Item returnItem;
	private final Block returnBlock;

	/**
	 * Constructs a new fishing reward that <b>adds an <u>item</u> to the fisher's inventory.</b> MCA adds a few of these as such:
	 * <p>
	 * <code>
	 * 	new FishingReward(Items.wheat_seeds, false, 1 , 4)); <p>
	 * 	new FishingReward(Items.bucket, false, 1 , 1)); <p>
	 * 	new FishingReward(Items.diamond, true, 1, 1));<p>
	 * </code>
	 * 
	 * @param returnItem The item that will be added to the fisher's inventory.
	 * @param isEnhanced Is this reward only available when the fisher's experience is greater than or equal to lvl 20?
	 * @param minimumReturn The minimum amount added to the inventory.
	 * @param maximumReturn The maximum amoutn added to the inventory.
	 */
	public FishingReward(Item returnItem, boolean isEnhanced, int minimumReturn, int maximumReturn)
	{
		this.returnItem = returnItem;
		returnBlock = null;
		this.isEnhanced = isEnhanced;
		this.minimumReturn = minimumReturn;
		this.maximumReturn = maximumReturn;
	}

	/**
	 * Constructs a new fishing reward that <b>adds a <u>block</u> to the fisher's inventory.</b> MCA adds a few of these as such:
	 * <p>
	 * <code>
	 * 	new FishingReward(Blocks.torch, false, 1 , 4)); <p>
	 * 	new FishingReward(Blocks.rail, false, 1 , 1)); <p>
	 * 	new FishingReward(Blocks.tnt, true, 1, 2));<p>
	 * </code>
	 * 
	 * @param returnBlock The block that will be added to the fisher's inventory.
	 * @param isEnhanced Is this reward only available when the fisher's experience is greater than or equal to lvl 20?
	 * @param minimumReturn The minimum amount added to the inventory.
	 * @param maximumReturn The maximum amoutn added to the inventory.
	 */
	public FishingReward(Block returnBlock, boolean isEnhanced, int minimumReturn, int maximumReturn)
	{
		returnItem = null;
		this.returnBlock = returnBlock;
		this.isEnhanced = isEnhanced;
		this.minimumReturn = minimumReturn;
		this.maximumReturn = maximumReturn;
	}

	public boolean getIsEnhanced()
	{
		return isEnhanced;
	}

	public boolean isBlock()
	{
		return returnBlock != null;
	}

	public Item getItem()
	{
		return returnItem;
	}

	public Block getBlock()
	{
		return returnBlock;
	}

	public int getMinimumReturn()
	{
		return minimumReturn;
	}

	public int getMaximumReturn()
	{
		return maximumReturn;
	}
}

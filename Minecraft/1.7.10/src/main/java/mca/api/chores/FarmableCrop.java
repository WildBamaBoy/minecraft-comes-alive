/*******************************************************************************
 * FarmableCrop.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import mca.api.enums.EnumFarmType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * A crop that can be farmed (both in Create Farm and Maintain mode) by an MCA villager.
 */
public class FarmableCrop
{
	private final String cropName;
	private final Item itemSeed;
	private final Block blockCrop;
	private final Block blockGrown;
	private final Block blockYield;
	private final Item itemYield;
	private final int minimumYield;
	private final int maximumYield;
	private final EnumFarmType farmType;
	private final boolean returnsSeeds;

	/**
	 * Constructs a new farmable crop that <b>produces a block that returns items when harvested</b> This is the same behavior as Minecraft's melon/it breaks into individual melons when harvested.
	 * <p>
	 * Ex:
	 * <p>
	 * <code>
	 * new FarmableCrop("Melon", Items.melon_seeds, Blocks.melon_stem, Blocks.melon_block, Items.melon, 3, 7, EnumFarmType.BLOCK, false));
	 * <code>
	 * 
	 * @param cropName The name of the crop. Will be displayed on the crop selection button in the farming GUI.
	 * @param itemSeed The item consumed as seeds for this crop.
	 * @param blockCrop The block planted by the farmer.
	 * @param blockGrown The block produced from the crop. For example, a melon stem produces a melon block.
	 * @param itemYield The item returned to the farmer's inventory after harvesting the blockGrown.
	 * @param minimumYield The minimum amount of crops that can be returned from a harvest.
	 * @param maximumYield The maximum amount of crops that can be returned from a harvest. This is modified by chore experience.
	 * @param farmType The farm type that the farmer should create when using this farmable crop.
	 * @param returnsSeeds Does this crop return some seeds to the farmer? (Like wheat)
	 */
	public FarmableCrop(String cropName, Item itemSeed, Block blockCrop, Block blockGrown, Item itemYield, int minimumYield, int maximumYield, EnumFarmType farmType, boolean returnsSeeds)
	{
		this.cropName = cropName;
		this.itemSeed = itemSeed;
		this.blockCrop = blockCrop;
		this.blockGrown = blockGrown;
		blockYield = null;
		this.itemYield = itemYield;
		this.minimumYield = minimumYield;
		this.maximumYield = maximumYield;
		this.farmType = farmType;
		this.returnsSeeds = returnsSeeds;
	}

	/**
	 * Constructs a new farmable crop that <b>produces a block that returns itself when harvested</b>. This is the same behavior as Minecraft's pumpkin/you receive the pumpkin block after harvesting.
	 * <p>
	 * Ex:
	 * <p>
	 * <code>
	 * new FarmableCrop("Pumpkin", Items.pumpkin_seeds, Blocks.pumpkin_stem, Blocks.pumpkin, 1, 1, EnumFarmType.BLOCK, false));
	 * <code>
	 * 
	 * @param cropName The name of the crop. Will be displayed on the crop selection button in the farming GUI.
	 * @param itemSeed The item consumed as seeds for this crop.
	 * @param blockCrop The block planted by the farmer.
	 * @param blockYield The block returned to the farmer's inventory after harvesting.
	 * @param minimumYield The minimum amount of crops that can be returned from a harvest.
	 * @param maximumYield The maximum amount of crops that can be returned from a harvest. This is modified by chore experience.
	 * @param farmType The farm type that the farmer should create when using this farmable crop.
	 * @param returnsSeeds Does this crop return some seeds to the farmer? (Like wheat)
	 */
	public FarmableCrop(String cropName, Item itemSeed, Block blockCrop, Block blockYield, int minimumYield, int maximumYield, EnumFarmType farmType, boolean returnsSeeds)
	{
		this.cropName = cropName;
		this.itemSeed = itemSeed;
		this.blockCrop = blockCrop;
		blockGrown = blockCrop;
		this.blockYield = blockYield;
		itemYield = null;
		this.minimumYield = minimumYield;
		this.maximumYield = maximumYield;
		this.farmType = farmType;
		this.returnsSeeds = returnsSeeds;
	}

	/**
	 * Constructs a new farmable crop that <b>does not produce another block/it grows and is harvested on the same tile</b>. This is the same behavior as Minecraft's wheat, carrots, and potatos. You will most likely be using this constructor.
	 * <p>
	 * Ex:
	 * <p>
	 * <code>
	 * new FarmableCrop("Wheat", Items.wheat_seeds, Blocks.wheat, Items.wheat, 1, 1, EnumFarmType.NORMAL, true));
	 * <code>
	 * 
	 * @param cropName The name of the crop. Will be displayed on the crop selection button in the farming GUI.
	 * @param itemSeed The item consumed as seeds for this crop.
	 * @param blockCrop The block planted by the farmer.
	 * @param itemYield The item returned to the farmer's inventory after harvesting.
	 * @param minimumYield The minimum amount of crops that can be returned from a harvest.
	 * @param maximumYield The maximum amount of crops that can be returned from a harvest. This is modified by chore experience.
	 * @param farmType The farm type that the farmer should create when using this farmable crop.
	 * @param returnsSeeds Does this crop return some seeds to the farmer? (Like wheat)
	 */
	public FarmableCrop(String cropName, Item itemSeed, Block blockCrop, Item itemYield, int minimumYield, int maximumYield, EnumFarmType farmType, boolean returnsSeeds)
	{
		this.cropName = cropName;
		this.itemSeed = itemSeed;
		this.blockCrop = blockCrop;
		blockGrown = blockCrop;
		blockYield = null;
		this.itemYield = itemYield;
		this.minimumYield = minimumYield;
		this.maximumYield = maximumYield;
		this.farmType = farmType;
		this.returnsSeeds = returnsSeeds;
	}

	public String getCropName()
	{
		return cropName;
	}

	public Item getSeedItem()
	{
		return itemSeed;
	}

	public Block getBlockCrop()
	{
		return blockCrop;
	}

	public Block getBlockGrown()
	{
		return blockGrown;
	}

	public Block getBlockYield()
	{
		return blockYield;
	}

	public Item getItemYield()
	{
		return itemYield;
	}

	public boolean getYieldsBlock()
	{
		return blockYield != null;
	}

	public int getMinimumYield()
	{
		return minimumYield;
	}

	public int getMaximumYield()
	{
		return maximumYield;
	}

	public EnumFarmType getFarmType()
	{
		return farmType;
	}

	public boolean getReturnsSeeds()
	{
		return returnsSeeds;
	}
}

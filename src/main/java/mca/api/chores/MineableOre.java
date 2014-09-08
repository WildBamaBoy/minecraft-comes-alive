/*******************************************************************************
 * MineableOre.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * An ore that can be mined by an MCA villager.
 */
public class MineableOre
{
	private final String oreName;
	private final Block blockOre;
	private final Block blockOreYield;
	private final Item itemOreYield;
	private final int yieldDamage;
	private final int minimumYield;
	private final int maximumYield;

	/**
	 * Constructs an ore that <b>yields an <u>item</u> when it is harvested</b>. For example, diamond ore doesn't return the diamond ore block. It returns a diamond item.
	 * <p>
	 * <b>Ex: </b> <code>
	 * new MineableOre("Diamond", Blocks.diamond_ore, Items.diamond, 0, 1, 1);
	 * </code>
	 * 
	 * @param oreName The ore name, shown when selecting the ore in the Mining GUI and when passively mining.
	 * @param blockOre The ore's block that spawns in the world.
	 * @param itemOreYield The item that the ore returns when harvested.
	 * @param oreDamage The returned item's damage/metadata, for subitems like dye powder in vanilla Minecraft.
	 * @param minimumYield The minimum amount returned when the ore is harvested.
	 * @param maximumYield The maximum amount returned when the ore is harvested.
	 */
	public MineableOre(String oreName, Block blockOre, Item itemOreYield, int yieldDamage, int minimumYield, int maximumYield)
	{
		this.oreName = oreName;
		this.blockOre = blockOre;
		blockOreYield = null;
		this.itemOreYield = itemOreYield;
		this.yieldDamage = yieldDamage;
		this.minimumYield = minimumYield;
		this.maximumYield = maximumYield;
	}

	/**
	 * Constructs an ore that <b>yields a <u>block</u> when it is harvested</b>. For example, some ores return themselves when harvested, like iron.
	 * <p>
	 * <b>Ex: </b> <code>
	 * new MineableOre("Iron", Blocks.iron_ore, Blocks.iron_ore, 0, 1, 1);
	 * </code>
	 * 
	 * @param oreName The ore name, shown when selecting the ore in the Mining GUI and when passively mining.
	 * @param blockOre The ore's block that spawns in the world.
	 * @param blockOreYield The block that the ore returns when harvested.
	 * @param oreDamage The returned item's damage/metadata, for subitems like dye powder in vanilla Minecraft.
	 * @param minimumYield The minimum amount returned when the ore is harvested.
	 * @param maximumYield The maximum amount returned when the ore is harvested.
	 */
	public MineableOre(String oreName, Block blockOre, Block blockOreYield, int oreDamage, int minimumYield, int maximumYield)
	{
		this.oreName = oreName;
		this.blockOre = blockOre;
		this.blockOreYield = blockOreYield;
		itemOreYield = null;
		yieldDamage = oreDamage;
		this.minimumYield = minimumYield;
		this.maximumYield = maximumYield;
	}

	public String getOreName()
	{
		return oreName;
	}

	public Block getOreBlock()
	{
		return blockOre;
	}

	public Block getOreBlockYield()
	{
		return blockOreYield;
	}

	public Item getOreItemYield()
	{
		return itemOreYield;
	}

	public boolean getYieldsBlock()
	{
		return blockOreYield != null;
	}

	public int getOreDamage()
	{
		return yieldDamage;
	}

	public int getMinimumReturn()
	{
		return minimumYield;
	}

	public int getMaximumReturn()
	{
		return maximumYield;
	}
}

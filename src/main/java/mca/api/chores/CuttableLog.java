/*******************************************************************************
 * CuttableLog.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import net.minecraft.block.Block;

/**
 * A log that can be cut by an MCA villager.
 */
public class CuttableLog
{
	private final String logName;
	private final Block blockLog;
	private final int blockDamage;

	/**
	 * Constructs a new cuttable log. MCA adds some of the vanilla Minecraft logs as such:
	 * <p>
	 * <code>
	 * ChoreRegistry.registerChoreEntry(new CuttableLog("Oak", Blocks.log, 0)); <p>
	 * ChoreRegistry.registerChoreEntry(new CuttableLog("Birch", Blocks.log, 2));<p>
	 * ChoreRegistry.registerChoreEntry(new CuttableLog("Dark Oak", Blocks.log2, 1)); <p>
	 * </code>
	 * 
	 * @param treeName The name of the log. It will be shown on the button used to select the desired tree type.
	 * @param blockLog The log that will be searched for, cut, and added to the woodcutter's inventory.
	 * @param blockDamage The damage value of the provided log, for subitems. Use 0 if you don't have any subitems.
	 */
	public CuttableLog(String logName, Block blockLog, int blockDamage)
	{
		this.logName = logName;
		this.blockLog = blockLog;
		this.blockDamage = blockDamage;
	}

	public String getTreeName()
	{
		return logName;
	}

	public Block getLogBlock()
	{
		return blockLog;
	}

	public int getLogDamage()
	{
		return blockDamage;
	}
}

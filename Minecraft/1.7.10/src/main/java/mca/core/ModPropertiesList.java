/*******************************************************************************
 * ModPropertiesList.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import java.io.Serializable;

/**
 * Contains the fields used by the mod properties manager.
 */
public class ModPropertiesList implements Serializable
{	
	/** The amount of time in minutes that it takes for a baby to grow up.*/
	public int babyGrowUpTimeMinutes = 10;
	
	/** The amount of time in minutes that it takes for a kid to grow up.*/
	public int kidGrowUpTimeMinutes = 180;

	/** Limit number of children per player on a server. */
	public int server_childLimit = -1;
	
	/** Should the farming chore be allowed to run? */
	public boolean server_allowFarmingChore = true;
	
	/** Should the fishing chore be allowed to run? */
	public boolean server_allowFishingChore = true;
	
	/** Should the woodcutting chore be allowed to run? */
	public boolean server_allowWoodcuttingChore = true;
	
	/** Should the mining chore be allowed to run? */
	public boolean server_allowMiningChore = true;
	
	/** Should the hunting chore be allowed to run? */
	public boolean server_allowHuntingChore = true;
	
	/** Should players be allowed to demand gifts from people? */
	public boolean server_allowDemandGift = true;
	
	/** How many villagers there must be in order for one guard to spawn. */
	public int guardSpawnRate = 3;
	
	/** A villager's base amount of maximum health. Guards have twice as much. */
	public int villagerBaseHealth = 20;
	
	/** Should children growth be halted? */
	public boolean haltChildGrowth = false;
	
	/** The prefix to append to villagers' chat */
	public String villagerChatPrefix = "";
}
/*******************************************************************************
 * ModPropertiesList.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.io;

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
	
	/** The item ID of the engagement ring.*/
	public int itemID_EngagementRing = 25650;
	
	/** The item ID of the wedding band.*/
	public int itemID_WeddingRing = 25651;
	
	/** The item ID of the arranger's ring.*/
	public int itemID_ArrangersRing = 25652;
	
	/** The item ID of the baby boy.*/
	public int itemID_BabyBoy = 25653;
	
	/** The item ID of the baby girl.*/
	public int itemID_BabyGirl = 25654;
	
	/** The item ID of the tombstone.*/
	public int itemID_Tombstone = 25655;
	
	/** The item ID of the male spawner egg.*/
	public int itemID_EggMale = 25656;
	
	/** The item ID of the female spawner egg.*/
	public int itemID_EggFemale = 25657;
	
	/** The item ID of the whistle.*/
	public int itemID_Whistle = 25658;

	/** The item ID of the villager editor.*/
	public int itemID_VillagerEditor = 25659;
	
	/** The item ID of the lost relative document.*/
	public int itemID_LostRelativeDocument = 25670;
	
	/** The item ID of the crown.*/
	public int itemID_Crown = 25671;
	
	/** The item ID of the heir crown.*/
	public int itemID_HeirCrown = 25672;

	/** The item ID of the king coat.*/
	public int itemID_KingsCoat = 25673;
	
	/** The item ID of the kings pants.*/
	public int itemID_KingsPants = 25674;
	
	/** The item ID of the kings boots.*/
	public int itemID_KingsBoots = 25675;
	
	/** The item ID of the red crown.*/
	public int itemID_RedCrown = 25676;

	/** The item ID of the green crown.*/
	public int itemID_GreenCrown = 25677;
	
	/** The item ID of the blue crown.*/
	public int itemID_BlueCrown = 25678;

	/** The item ID of the pink crown.*/
	public int itemID_PinkCrown = 25679;
	
	/** The item ID of the purple crown.*/
	public int itemID_PurpleCrown = 25680;
	
	/** The block ID of the tombstone.*/
	public int blockID_Tombstone = 2388;

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
	
	/** Should MCA display update notifications? */
	public boolean checkForUpdates = true;
	
	/** The last update found by MCA. */
	public String lastFoundUpdate = "";
	
	/** How many villagers there must be in order for one guard to spawn. */
	public int guardSpawnRate = 3;
	
	/** Should packet data be compressed before being sent? */
	public boolean compressPackets = true;
	
	/** Should children growth be halted? */
	public boolean haltChildGrowth = false;
}
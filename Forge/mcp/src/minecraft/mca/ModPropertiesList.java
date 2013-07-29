/*******************************************************************************
 * ModPropertiesList.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.io.Serializable;

/**
 * Contains the fields used by the mod properties manager.
 */
public class ModPropertiesList implements Serializable
{	
	/** The amount of time in minutes that it takes for a baby to grow up.*/
	public int babyGrowUpTimeMinutes = 10;
	
	/** The amount of time in minutes that it takes for a kid to grow up.*/
	public int kidGrowUpTimeMinutes = 60;
	
	/** The item ID of the engagement ring.*/
	public int itemID_EngagementRing = 721;
	
	/** The item ID of the wedding band.*/
	public int itemID_WeddingRing = 722;
	
	/** The item ID of the arranger's ring.*/
	public int itemID_ArrangersRing = 723;
	
	/** The item ID of the baby boy.*/
	public int itemID_BabyBoy = 724;
	
	/** The item ID of the baby girl.*/
	public int itemID_BabyGirl = 725;
	
	/** The item ID of the tombstone.*/
	public int itemID_Tombstone = 726;
	
	/** The item ID of the male spawner egg.*/
	public int itemID_EggMale = 727;
	
	/** The item ID of the female spawner egg.*/
	public int itemID_EggFemale = 728;
	
	/** The item ID of the whistle.*/
	public int itemID_Whistle = 729;
	
	/** The item ID of the fertility potion.*/
	public int itemID_FertilityPotion = 731;

	/** The item ID of the villager editor.*/
	public int itemID_VillagerEditor = 732;
	
	/** The item ID of the lost relative document.*/
	public int itemID_LostRelativeDocument = 733;
	
	/** The item ID of the crown.*/
	public int itemID_Crown = 734;
	
	/** The item ID of the heir crown.*/
	public int itemID_HeirCrown = 735;

	/** The item ID of the king coat.*/
	public int itemID_KingsCoat = 736;
	
	/** The item ID of the kings pants.*/
	public int itemID_KingsPants = 737;
	
	/** The item ID of the kings boots.*/
	public int itemID_KingsBoots = 738;
	
	/** The block ID of the tombstone.*/
	public int blockID_Tombstone = 205;

	/** Limit number of children per player on a server. */
	public int server_childLimit = -1;
	
//	/** Should a dedicated server overwrite Testificates? */
//	public boolean server_overwriteTestificates = true;
	
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
	
	/** How many villagers there must be in order for one guard to spawn. */
	public int guardSpawnRate = 3;
}
/*******************************************************************************
 * WorldPropertiesList.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the fields that are used by the world properties manager.
 */
public class WorldPropertiesList implements Serializable
{
	/**The selected gender of the player.*/
	public String  playerGender = "Male";
	
	/**The name of the player's baby.*/
	public String  babyName = "";
	
	/**The gender of the player's baby.*/
	public boolean babyIsMale = false;
	
	/**The selected name of the player.*/
	public String  playerName = "";
	
	/**MD5'd player's selected gender preference.*/
	public String genderPreference = "";
	
	/**Does the player have a baby?*/
	public boolean babyExists = false;
	
	/**Is the player's baby ready to grow up?*/
	public boolean babyReadyToGrow = false;
	
	/**The player's ID*/
	public int playerID	= 0;
	
	/**The MCA ID of the player's spouse.*/
	public int playerSpouseID = 0;
	
	/**The name of the player's spouse.*/
	public String playerSpouseName = "";
	
	/**How long the baby has existed.*/
	public int minutesBabyExisted = 0;
	
	/**Is the player engaged to someone?*/
	public boolean isEngaged = false;
	
	/**Does the player want the "Sleeping" tag to appear above MCA villagers?*/
	public boolean hideSleepingTag = false;
	
	/**Is "Lite" mode enabled for this player?*/
	public boolean isInLiteMode = false;
	
//	/**Does the player want testificates to be overwritten with MCA villagers?*/
//	public boolean overwriteTestificates = true;
	
	/**Should mood particles be displayed for anger, sadness, etc?*/
	public boolean displayMoodParticles = true;
	
	/**Should children grow up automatically?*/
	public boolean childrenGrowAutomatically = true;
	
	/**Should all marriage requests be blocked?*/
	public boolean blockMarriageRequests = false;
	
	/**Should name tags be rendered above a villager?*/
	public boolean showNameTags = true;
	
	/**The list containing all usernames whose marriage requests will be blocked.*/
	public List<String> blockList = new ArrayList<String>();
	
	/**Is the player a monarch? */
	public boolean isMonarch = false;
	
	/**The ID of the player's chosen heir. */
	public int heirId = -1;
	
	/**A stat keeping track of how many villagers the player has executed. */
	public int stat_villagersExecuted = 0;
	
	/**A stat keeping up with how many villagers the player has made into peasants. */
	public int stat_villagersMadePeasants = 0;
	
	/**A stat keeping up with how many guards the player has made into knights. */
	public int stat_guardsMadeKnights = 0;
	
	/**A stat keeping up with how many wives the player has executed. */
	public int stat_wivesExecuted = 0;
}

/*******************************************************************************
 * VillagerEntryMCA.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Object used to keep track of villager types registered with MCA.
 */
public class VillagerEntryMCA 
{
	/** A list of skins that can be used for male villagers. */
	public List<String> skinsMale = new ArrayList<String>();
	
	/** A list of skins that can be used for female villagers. */
	public List<String> skinsFemale = new ArrayList<String>();
	
	protected int id;
	protected boolean isLocalized;
	protected boolean useDefaultTexture;
	protected String professionName;
	protected String texturesLocation;

	/**
	 * Constructor
	 * 
	 * @param 	id					The profession ID of the villager.
	 * @param 	professionName		The villager's unlocalized profession name.
	 * @param 	texturesLocation	The folder containing skins that the villager can use.
	 */
	protected VillagerEntryMCA(int id, String professionName, String texturesLocation)
	{
		this.id = id;
		this.professionName = professionName;
		this.texturesLocation = texturesLocation;
		this.isLocalized = false;
		this.useDefaultTexture = this.texturesLocation.equals("/assets/mca/textures/api/skins/");
	}

	/**
	 * Used only by MCA at this time.
	 */
	public boolean getIsLocalized()
	{
		return isLocalized;
	}

	/**
	 * Used by MCA's skin loader to determine of the API's default textures should be used.
	 * 
	 * @return	True if the villager's texture wasn't provided. False if otherwise.
	 */
	public boolean getUseDefaultTexture()
	{
		return useDefaultTexture;
	}

	/**
	 * Used only by MCA at this time.
	 */
	public String getLocalizedProfessionID()
	{
		if (this.id != -1)
		{
			return "profession." + professionName.toLowerCase();
		}
		
		else
		{
			return "profession.playerchild";
		}
	}

	/**
	 * Gets the villager type name provided when it was registered.
	 * 
	 * @return	professionName value of this villager entry.
	 */
	public String getUnlocalizedProfessionName()
	{
		return professionName;
	}

	/**
	 * Gets the villager texture location provided when it was registered.
	 * 
	 * @return	Texture location provided to the villager registry. If a texture location was not provided,
	 * 			returns "/assets/mca/textures/api/skins/"
	 */
	public String getTexturesLocation() 
	{
		return texturesLocation;
	}
}

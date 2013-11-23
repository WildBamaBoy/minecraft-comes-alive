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

import mca.core.MCA;

/**
 * Object used to keep track of villager types registered with MCA.
 */
public class VillagerEntryMCA 
{
	/** A list of skins that can be used for male villagers. */
	private List<String> skinsMale = new ArrayList<String>();
	
	/** A list of skins that can be used for female villagers. */
	private List<String> skinsFemale = new ArrayList<String>();
	
	public final int id;
	public final boolean useDefaultTexture;
	public final String professionName;
	public final String texturesLocation;
	private boolean isLocalized;
	
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
	 * Used only by MCA at this time.
	 */
	public void setIsLocalized(boolean value)
	{
		this.isLocalized = value;
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
	
	/**
	 * Adds the location of a texture to this entry's male skin list.
	 * 
	 * @param 	skinLocation	The location of the male skin texture.
	 */
	public void addMaleSkin(String skinLocation)
	{
		this.skinsMale.add(skinLocation);
	}
	
	/**
	 * Adds the location of a texture to this entry's female skin list.
	 * 
	 * @param 	skinLocation	The location of the female skin texture.
	 */
	public void addFemaleSkin(String skinLocation)
	{
		this.skinsFemale.add(skinLocation);
	}
	
	/**
	 * Gets a random male skin appropriate from this entry's male skin list.
	 * 
	 * @return	Random male skin stored in male skin list.
	 */
	public String getRandomMaleSkin()
	{
		return this.skinsMale.get(MCA.instance.rand.nextInt(skinsMale.size()));
	}
	
	/**
	 * Gets a random female skin appropriate from this entry's female skin list.
	 * 
	 * @return	Random female skin stored in female skin list.
	 */
	public String getRandomFemaleSkin()
	{
		return this.skinsFemale.get(MCA.instance.rand.nextInt(skinsFemale.size()));
	}
	
	/**
	 * Returns reference to the male skins list.
	 * 
	 * @return	The male skin list contained in this entry.
	 */
	public List<String> getMaleSkinsList()
	{
		return this.skinsMale;
	}
	
	/**
	 * Returns reference to the female skins list.
	 * 
	 * @return	The female skin list contained in this entry.
	 */
	public List<String> getFemaleSkinsList()
	{
		return this.skinsFemale;
	}
}

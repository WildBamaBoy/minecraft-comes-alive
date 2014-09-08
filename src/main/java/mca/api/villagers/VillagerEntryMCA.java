/*******************************************************************************
 * VillagerEntryMCA.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.villagers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Object used to keep track of villager types registered with MCA.
 */
public final class VillagerEntryMCA
{
	/** A list of skins that can be used for male villagers. */
	private final List<String> skinsMale = new ArrayList<String>();

	/** A list of skins that can be used for female villagers. */
	private final List<String> skinsFemale = new ArrayList<String>();

	public final int professionId;
	public final boolean useDefaultTexture;
	public final String professionName;
	public final String texturesLocation;
	public final String modId;
	private boolean useLocalizedForm;

	/**
	 * Constructor
	 * 
	 * @param professionId The profession ID of the villager.
	 * @param professionName The villager's unlocalized profession name.
	 * @param texturesLocation The folder containing skins that the villager can use.
	 * @param modId The mod ID of the mod the villager belongs to. This is the ID you use to access your mod's assets.
	 */
	public VillagerEntryMCA(int professionId, String professionName, String texturesLocation, String modId)
	{
		this.professionId = professionId;
		this.professionName = professionName;
		this.texturesLocation = texturesLocation;
		this.modId = modId;
		useLocalizedForm = false;
		useDefaultTexture = this.texturesLocation.equals("/assets/mca/textures/api/skins/");
	}

	/**
	 * Used only by MCA at this time.
	 */
	public boolean isLocalized()
	{
		return useLocalizedForm;
	}

	/**
	 * Used only by MCA at this time.
	 */
	public void setIsLocalized(boolean value)
	{
		useLocalizedForm = value;
	}

	/**
	 * Used by MCA's skin loader to determine of the API's default textures should be used.
	 * 
	 * @return True if the villager's texture wasn't provided. False if otherwise.
	 */
	public boolean isDefaultTextureUsed()
	{
		return useDefaultTexture;
	}

	/**
	 * Used only by MCA at this time.
	 */
	public String getLocalizedProfessionID()
	{
		if (professionId == -1)
		{
			return "profession.playerchild";
		}

		else
		{
			return "profession." + professionName.toLowerCase();
		}
	}

	/**
	 * Gets the villager type name provided when it was registered.
	 * 
	 * @return professionName value of this villager entry.
	 */
	public String getUnlocalizedProfessionName()
	{
		return professionName;
	}

	/**
	 * Gets the villager texture location provided when it was registered.
	 * 
	 * @return Texture location provided to the villager registry. If a texture location was not provided, returns "/assets/mca/textures/api/skins/"
	 */
	public String getTexturesLocation()
	{
		return texturesLocation;
	}

	/**
	 * Adds the location of a texture to this entry's male skin list.
	 * 
	 * @param skinLocation The location of the male skin texture.
	 */
	public void addMaleSkin(String skinLocation)
	{
		skinsMale.add(skinLocation);
	}

	/**
	 * Adds the location of a texture to this entry's female skin list.
	 * 
	 * @param skinLocation The location of the female skin texture.
	 */
	public void addFemaleSkin(String skinLocation)
	{
		skinsFemale.add(skinLocation);
	}

	/**
	 * Gets a random male skin appropriate from this entry's male skin list.
	 * 
	 * @return Random male skin stored in male skin list.
	 */
	public String getRandomMaleSkin()
	{
		return skinsMale.get(new Random().nextInt(skinsMale.size()));
	}

	/**
	 * Gets a random female skin appropriate from this entry's female skin list.
	 * 
	 * @return Random female skin stored in female skin list.
	 */
	public String getRandomFemaleSkin()
	{
		return skinsFemale.get(new Random().nextInt(skinsFemale.size()));
	}

	/**
	 * Returns reference to the male skins list.
	 * 
	 * @return The male skin list contained in this entry.
	 */
	public List<String> getMaleSkinsList()
	{
		return skinsMale;
	}

	/**
	 * Returns reference to the female skins list.
	 * 
	 * @return The female skin list contained in this entry.
	 */
	public List<String> getFemaleSkinsList()
	{
		return skinsFemale;
	}

	/**
	 * @return The mod ID of the mod this entry belongs to.
	 */
	public String getModId()
	{
		return modId;
	}
}

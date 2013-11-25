/*******************************************************************************
 * VillagerRegistryMCA.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls the types of villagers within MCA.
 */
public final class VillagerRegistryMCA 
{
	private static Map<Integer, VillagerEntryMCA> registeredVillagersMap = new HashMap<Integer, VillagerEntryMCA>();

	private VillagerRegistryMCA() { }
	
	/**
	 * Registers a villager of the provided id with MCA. Profession name defaults to "Villager" and
	 * texture defaults to the ones included with the MCA API.
	 * 
	 * @param 	professionId	The numeric profession ID of the villager. Cannot be -1 - 7.
	 */
	public static void registerVillagerType(int professionId)
	{
		registerVillagerType(professionId, "Villager");
	}

	/**
	 * Registers a villager of the provided ID and profession name with MCA. Texture defaults to the ones
	 * included with the MCA API.
	 * 
	 * @param 	professionId	The numeric profession ID of the villager. Cannot be -1 - 7.
	 * @param 	professionName	String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 */
	public static void registerVillagerType(int professionId, String professionName)
	{
		registerVillagerType(professionId, professionName, "/assets/mca/textures/api/skins/", null);
	}
	
	/**
	 * Registers a villager of the provided ID, profession name, and texture location with MCA.
	 * 
	 * @param 	professionId	The numeric ID of the villager. Cannot be -1 - 7.
	 * @param 	professionName	String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 * @param 	textureLocation	The directory containing the skins usable by this villager.
	 */
	public static void registerVillagerType(int professionId, String professionName, String textureLocation)
	{
		registerVillagerType(professionId, professionName, textureLocation, null);
	}

	/**
	 * Used only by MCA to register default professions. Do not use this method.
	 */
	public static void registerVillagerType(int professionId, String professionName, Class registeringClass)
	{
		registerVillagerType(professionId, professionName, "/assets/mca/textures/skins/", registeringClass);
	}
	
	private static void registerVillagerType(int professionId, String professionName, String textureLocation, Class registeringClass)
	{
		if (registeredVillagersMap.containsKey(professionId))
		{
			throw new IllegalArgumentException("Villager ID #" + professionId + " is already registered as \"" + professionName + "\"");
		}
		
		else
		{
			if (registeringClass == null)
			{
				if (professionId >= -1 && professionId <= 7)
				{
					throw new IllegalArgumentException("Villager IDs -1 through 7 are reserved for Minecraft Comes Alive.");
				}
				
				else
				{
					registeredVillagersMap.put(professionId, new VillagerEntryMCA(professionId, professionName, textureLocation));
				}
			}
			
			else
			{
				if (registeringClass.getSimpleName().equals("MCA"))
				{
					final VillagerEntryMCA entry = new VillagerEntryMCA(professionId, professionName, "/assets/mca/textures/skins/");
					entry.setIsLocalized(true);
					registeredVillagersMap.put(professionId, entry);
				}
				
				else
				{
					if (professionId >= -1 && professionId <= 7)
					{
						throw new IllegalArgumentException("Villager IDs -1 through 7 are reserved for Minecraft Comes Alive.");
					}
					
					else
					{
						registeredVillagersMap.put(professionId, new VillagerEntryMCA(professionId, professionName, textureLocation));
					}
				}
			}
		}
	}

	/**
	 * Gets the villager entry with the provided ID from the registered villagers map.
	 * 
	 * @param 	professionId	The villager ID whose entry will be retrieved.
	 * 
	 * @return	VillagerEntryMCA object with the provided ID.
	 */
	public static VillagerEntryMCA getRegisteredVillagerEntry(int professionId)
	{
		return registeredVillagersMap.get(professionId);
	}
	
	/**
	 * Gets total number of villagers registered with MCA.
	 * 
	 * @return	The size of the registered villagers map.
	 */
	public static int getNumberOfRegisteredVillagers()
	{
		return registeredVillagersMap.size();
	}

	/**
	 * Gets the registered villagers map.
	 * 
	 * @return	The villager registry's registered villagers map.
	 */
	public static Map<Integer, VillagerEntryMCA> getRegisteredVillagersMap() 
	{
		return registeredVillagersMap;
	}
}

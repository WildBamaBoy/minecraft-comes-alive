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

	/**
	 * Registers a villager of the provided id with MCA. Profession name defaults to "Villager" and
	 * texture defaults to the ones included with the MCA API.
	 * 
	 * @param 	id	The numeric ID of the villager. Cannot be -1 - 7.
	 */
	public static void registerVillagerType(int id)
	{
		registerVillagerType(id, "Villager");
	}

	/**
	 * Registers a villager of the provided ID and profession name with MCA. Texture defaults to the ones
	 * included with the MCA API.
	 * 
	 * @param 	id				The numeric ID of the villager. Cannot be -1 - 7.
	 * @param 	professionName	String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 */
	public static void registerVillagerType(int id, String professionName)
	{
		registerVillagerType(id, professionName, "/assets/mca/textures/api/skins/", null);
	}
	
	/**
	 * Registers a villager of the provided ID, profession name, and texture location with MCA.
	 * 
	 * @param 	id				The numeric ID of the villager. Cannot be -1 - 7.
	 * @param 	professionName	String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 * @param 	textureLocation	The directory containing the skins usable by this villager.
	 */
	public static void registerVillagerType(int id, String professionName, String textureLocation)
	{
		registerVillagerType(id, professionName, textureLocation, null);
	}

	/**
	 * Used only by MCA to register default professions. Do not use this method.
	 */
	public static void registerVillagerType(int id, String professionName, Class registeringClass)
	{
		registerVillagerType(id, professionName, "/assets/mca/textures/api/skins/", registeringClass);
	}
	
	private static void registerVillagerType(int id, String professionName, String textureLocation, Class registeringClass)
	{
		if (!registeredVillagersMap.containsKey(id))
		{
			if (registeringClass == null)
			{
				if (id >= -1 && id <= 7)
				{
					throw new IllegalArgumentException("Villager IDs -1 through 7 are reserved for Minecraft Comes Alive.");
				}
				
				else
				{
					registeredVillagersMap.put(id, new VillagerEntryMCA(id, professionName, textureLocation));
				}
			}
			
			else
			{
				if (registeringClass.getSimpleName().equals("MCA"))
				{
					VillagerEntryMCA entry = new VillagerEntryMCA(id, professionName, "/assets/mca/textures/skins/");
					entry.isLocalized = true;
					registeredVillagersMap.put(id, entry);
				}
				
				else
				{
					if (id >= -1 && id <= 7)
					{
						throw new IllegalArgumentException("Villager IDs -1 through 7 are reserved for Minecraft Comes Alive.");
					}
					
					else
					{
						registeredVillagersMap.put(id, new VillagerEntryMCA(id, professionName, textureLocation));
					}
				}
			}
		}

		else
		{
			throw new IllegalArgumentException("Villager ID #" + id + " is already registered as \"" + professionName + "\"");
		}
	}

	/**
	 * Gets the villager entry with the provided ID from the registered villagers map.
	 * 
	 * @param 	id	The villager ID whose entry will be retrieved.
	 * 
	 * @return	VillagerEntryMCA object with the provided ID.
	 */
	public static VillagerEntryMCA getRegisteredVillagerEntry(int id)
	{
		return registeredVillagersMap.get(id);
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

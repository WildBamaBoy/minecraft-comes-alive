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
	 * Registers a villager of the provided type with MCA. Profession name defaults to "Villager" and
	 * texture defaults to the one included with the MCA API.
	 * 
	 * @param 	id	The numeric ID of the villager. Cannot be -1 - 7.
	 */
	public static void registerVillagerType(int id)
	{
		registerVillagerType(id, "Villager");
	}

	/**
	 * Registers a villager of the provided ID and profession name with MCA. Texture defaults to the one
	 * included with the MCA API.
	 * 
	 * @param 	id				The numeric ID of the villager. Cannot be -1 - 7.
	 * @param 	professionName	String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 */
	public static void registerVillagerType(int id, String professionName)
	{
		registerVillagerType(id, professionName, "/assets/mca/textures/api/skins/", null);
	}
	
	public static void registerVillagerType(int id, String professionName, String textureLocation)
	{
		registerVillagerType(id, professionName, textureLocation, null);
	}

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

	public static VillagerEntryMCA getRegisteredVillagerEntry(int id)
	{
		return registeredVillagersMap.get(id);
	}
	
	public static int getNumberOfRegisteredVillagers()
	{
		return registeredVillagersMap.size();
	}

	public static Map<Integer, VillagerEntryMCA> getRegisteredVillagersMap() 
	{
		return registeredVillagersMap;
	}
}

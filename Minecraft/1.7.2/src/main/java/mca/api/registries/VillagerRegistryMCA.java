/*******************************************************************************
 * VillagerRegistryMCA.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mca.api.villagers.AbstractVillagerPlugin;
import mca.api.villagers.VillagerEntryMCA;

/**
 * Controls the types of villagers within MCA.
 */
public final class VillagerRegistryMCA
{
	private static Map<Integer, VillagerEntryMCA> registeredVillagersMap = new HashMap<Integer, VillagerEntryMCA>();
	private static List<AbstractVillagerPlugin> registeredVillagerPlugins = new ArrayList<AbstractVillagerPlugin>();

	private VillagerRegistryMCA()
	{
	}

	/**
	 * Registers an object implementing IVillagerPlugin so that its additions to villagers will be applied.
	 * 
	 * @param pluginObject The object implementing IVillagerPlugin that is to be added to the registered villagers map.
	 */
	public static void registerVillagerPlugin(AbstractVillagerPlugin pluginObject)
	{
		registeredVillagerPlugins.add(pluginObject);
	}

	/**
	 * Registers a villager of the provided ID and profession name with MCA. Texture defaults to the ones included with the MCA API.
	 * 
	 * @param professionId The numeric profession ID of the villager. Cannot be -1 - 7.
	 * @param professionName String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 */
	public static void registerVillagerType(int professionId, String professionName)
	{
		registerVillagerType(professionId, professionName, "/assets/mca/textures/api/skins/", "mca", null);
	}

	/**
	 * Registers a villager of the provided ID, profession name, and texture location with MCA.
	 * 
	 * @param professionId The numeric ID of the villager. Cannot be -1 - 7.
	 * @param professionName String shown on MCA villager that identifies their profession. Ex.) %Name% the %ProfessionName%
	 * @param textureLocation The directory containing the skins usable by this villager.
	 */
	public static void registerVillagerType(int professionId, String professionName, String textureLocation, String modId)
	{
		registerVillagerType(professionId, professionName, textureLocation, modId, null);
	}

	/**
	 * Used only by MCA to register default professions. Do not use this method.
	 */
	public static void registerVillagerType(int professionId, String professionName, Class registeringClass)
	{
		registerVillagerType(professionId, professionName, "/assets/mca/textures/skins/", "mca", registeringClass);
	}

	private static void registerVillagerType(int professionId, String professionName, String textureLocation, String modId, Class registeringClass)
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
					registeredVillagersMap.put(professionId, new VillagerEntryMCA(professionId, professionName, textureLocation, modId));
				}
			}

			else
			{
				if (registeringClass.getSimpleName().equals("MCA"))
				{
					final VillagerEntryMCA entry = new VillagerEntryMCA(professionId, professionName, "/assets/mca/textures/skins/", "mca");
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
						registeredVillagersMap.put(professionId, new VillagerEntryMCA(professionId, professionName, textureLocation, modId));
					}
				}
			}
		}
	}

	/**
	 * Gets the villager entry with the provided ID from the registered villagers map.
	 * 
	 * @param professionId The villager ID whose entry will be retrieved.
	 * @return VillagerEntryMCA object with the provided ID.
	 */
	public static VillagerEntryMCA getRegisteredVillagerEntry(int professionId)
	{
		return registeredVillagersMap.get(professionId);
	}

	/**
	 * Gets total number of villagers registered with MCA.
	 * 
	 * @return The size of the registered villagers map.
	 */
	public static int getNumberOfRegisteredVillagers()
	{
		return registeredVillagersMap.size();
	}

	/**
	 * Gets the registered villagers map.
	 * 
	 * @return The villager registry's registered villagers map.
	 */
	public static Map<Integer, VillagerEntryMCA> getRegisteredVillagersMap()
	{
		return registeredVillagersMap;
	}

	/**
	 * Gets the registered villager plugins list.
	 * 
	 * @return The villager registry's registered villager plugins list.
	 */
	public static List<AbstractVillagerPlugin> getRegisteredVillagerPlugins()
	{
		return registeredVillagerPlugins;
	}

	public static VillagerEntryMCA getRandomVillagerEntry()
	{
		final int index = new Random().nextInt(registeredVillagersMap.values().toArray().length);
		return registeredVillagersMap.get(registeredVillagersMap.keySet().toArray()[index]);
	}
}

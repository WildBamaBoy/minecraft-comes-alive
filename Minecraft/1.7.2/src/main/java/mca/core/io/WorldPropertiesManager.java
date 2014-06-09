/*******************************************************************************
 * WorldPropertiesManager.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import mca.core.MCA;
import mca.enums.EnumPacketType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.radixshock.radixcore.core.RadixCore;
import com.radixshock.radixcore.network.Packet;
import com.radixshock.radixcore.util.FileSystemOperations;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * Handles operations that use the WorldProps.properties file.
 */
public class WorldPropertiesManager implements Serializable
{
	private String currentPlayerName = "";
	private String currentWorldName = "";
	private File worldPropertiesFolderPath = null;
	private File worldPropertiesFolder = null;
	private File worldPropertiesFile = null;
	private transient Properties properties = new Properties();
	private transient FileInputStream inputStream   = null;
	private transient FileOutputStream outputStream = null;

	/** The properties and values stored within the world properties file. */
	public WorldPropertiesList worldProperties = new WorldPropertiesList();

	/**
	 * Constructor
	 * 
	 * @param 	worldName	The name of the world this manager will be used in.
	 * @param 	playerName	The name of the player this manager belongs to.
	 */
	public WorldPropertiesManager(String worldName, String playerName)
	{
		MinecraftServer server = MinecraftServer.getServer();

		//Assign relevant data.
		currentPlayerName = playerName;
		currentWorldName = worldName;

		if (server.isDedicatedServer())
		{
			worldPropertiesFolderPath   = new File(RadixCore.getInstance().runningDirectory + "/config/MCA/ServerWorlds/");
			worldPropertiesFolder   	= new File(worldPropertiesFolderPath.getPath() + "/" + worldName + "/" + playerName + "/");
			worldPropertiesFile     	= new File(worldPropertiesFolder.getPath() + "/" + "ServerWorldProps.properties");
		}

		else
		{
			worldPropertiesFolderPath   = new File(RadixCore.getInstance().runningDirectory + "/config/MCA/Worlds/");
			worldPropertiesFolder   	= new File(worldPropertiesFolderPath.getPath() + "/" + worldName + "/" + playerName + "/");
			worldPropertiesFile     	= new File(worldPropertiesFolder.getPath() + "/" + "WorldProps.properties");
		}

		//Account for issues with Mac's god-awful OS.
		if (worldPropertiesFile.getPath().contains("/.DS_STORE"))
		{
			worldPropertiesFile = new File(worldPropertiesFile.getPath().replace("/.DS_STORE", ""));
		}
		
		//Check and be sure the config/MCA/Worlds folder exists.
		if (worldPropertiesFolderPath.exists() == false)
		{
			worldPropertiesFolderPath.mkdir();
			MCA.getInstance().getLogger().log("Created Worlds folder.");
		}

		//Now check if the current world has its own properties folder.
		if (worldPropertiesFolder.exists() == false)
		{
			worldPropertiesFolder.mkdirs();
			MCA.getInstance().getLogger().log("Created new properties folder for the world '" + worldName + "'.");

		}

		//Then check if the world has a properties file within that folder. If it doesn't, a new one is created.
		if (worldPropertiesFile.exists() == false)
		{
			worldProperties.playerID = (int) Math.abs((playerName.hashCode() + System.currentTimeMillis() % (1024 * 1024))) * -1;
			
			saveWorldProperties();
			MCA.getInstance().getLogger().log("Saved new world properties for world '" + worldName + "' and player '" + playerName + "'.");
		}

		//If the world does have its own world properties file, then load it.
		else
		{
			loadWorldProperties();
			MCA.getInstance().getLogger().log("Loaded existing world properties for world '" + worldName + "' and player '" + playerName + "'.");
		}
	}

	/**
	 * Saves all world properties to the WorldProps.properties file.
	 */
	public void saveWorldProperties()
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			try
			{
				properties = new Properties();

				//Put world specific data in the properties.
				for (Field f : WorldPropertiesList.class.getFields())
				{
					try
					{
						String fieldType = f.getType().toString();

						if (fieldType.contains("List"))
						{
							String stringToSave = "";

							for (String s : worldProperties.blockList)
							{
								stringToSave += s;

								if (worldProperties.blockList.indexOf(s) != worldProperties.blockList.size() - 1)
								{
									stringToSave += ",";
								}
							}

							properties.put(f.getName(), stringToSave);
						}

						else if (fieldType.contains("boolean") || fieldType.contains("int") || fieldType.contains("String"))
						{
							properties.put(f.getName(), f.get(worldProperties).toString());
						}
					}

					catch (NullPointerException e)
					{
						MCA.getInstance().getLogger().log(e);
						continue;
					}
				}

				//Store the variables in the properties instance to file.
				outputStream = new FileOutputStream(worldPropertiesFile);
				properties.store(outputStream, "MCA Properties for World: " + currentWorldName);
				outputStream.close();

				MCA.getInstance().getLogger().log("Saved world properties for player " + currentPlayerName + " in world " + currentWorldName);
				MCA.getInstance().playerWorldManagerMap.put(currentPlayerName, this);

				//Load properties again to update them across the client and integrated server.
				loadWorldProperties();

				//Send the properties to the server or client.
				if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				{
					MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetWorldProperties, this));
				}

				else if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
				{
					MCA.packetPipeline.sendPacketToPlayer(new Packet(EnumPacketType.SetWorldProperties, this), (EntityPlayerMP)MCA.getInstance().getPlayerByName(currentPlayerName));
				}
			}

			catch (FileNotFoundException e)
			{
				//Check for the rare "The requested operation cannot be performed on a file with a user-mapped section open"
				//message. Skip saving if it's encountered.
				if (!e.getMessage().contains("user-mapped"))
				{
					RadixCore.getInstance().quitWithException("FileNotFoundException occurred while saving world properties to file.", e);
				}
			}

			catch (IllegalAccessException e)
			{
				RadixCore.getInstance().quitWithException("IllegalAccessException occurred while saving world properties to file.", e);
			}

			catch (IOException e)
			{
				return;
			}

			catch (NullPointerException e)
			{
				MCA.getInstance().getLogger().log(e);
				RadixCore.getInstance().quitWithException("NullPointerException while saving world properties.", e);
			}
		}

		else
		{
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetWorldProperties, this));
		}
	}

	/**
	 * Loads the WorldProps.properties file and assigns the values within to the appropriate fields in the world properties list.
	 */
	public void loadWorldProperties()
	{
		try
		{
			//Clear the properties instance and load the world's properties file.
			properties = new Properties();

			//Load the data from the world properties file into the properties instance.
			inputStream = new FileInputStream(worldPropertiesFile);
			properties.load(inputStream);
			inputStream.close();

			//Loop through all fields prefixed with world_ and assign their value that is in the properties.
			for (Field f : WorldPropertiesList.class.getFields())
			{
				String fieldType = f.getType().toString();

				//Determine the type of data contained in the field and parse it accordingly, since everything read from
				//the properties instance is a String.
				if (fieldType.contains("boolean"))
				{
					f.set(worldProperties, Boolean.parseBoolean(properties.getProperty(f.getName())));
				}

				else if (fieldType.contains("List"))
				{
					String listData = properties.getProperty(f.getName()).toString();

					List<String> list = new ArrayList<String>();

					for (String s : listData.split(","))
					{
						list.add(s);
					}

					f.set(worldProperties, list);
				}

				else if (fieldType.contains("int"))
				{
					f.set(worldProperties, Integer.parseInt(properties.getProperty(f.getName())));
				}

				else if (fieldType.contains("String"))
				{
					f.set(worldProperties, properties.getProperty(f.getName()));
				}
			}

			MCA.getInstance().playerWorldManagerMap.put(currentPlayerName, this);
		}

		catch (FileNotFoundException e)
		{
			RadixCore.getInstance().quitWithException("FileNotFoundException occurred while loading world properties from file.", e);
		}

		catch (IllegalAccessException e)
		{
			RadixCore.getInstance().quitWithException("IllegalAccessException occurred while loading world properties from file.", e);
		}

		catch (IOException e)
		{
			RadixCore.getInstance().quitWithException("IOException occurred while loading world properties from file.", e);
		}

		catch (NullPointerException e)
		{
			resetWorldProperties();
		}

		catch (NumberFormatException e)
		{
			resetWorldProperties();
		}
	}

	/**
	 * Resets all world properties back to their default values.
	 */
	public void resetWorldProperties()
	{
		//Retain the player's ID when resetting.
		int oldId = worldProperties.playerID;

		MCA.getInstance().getLogger().log(currentPlayerName + "'s world properties are errored. Resetting back to original settings.");
		worldProperties = new WorldPropertiesList();
		worldProperties.playerID = oldId;

		saveWorldProperties();
	}

	/**
	 * Deletes all world properties folders that do not have a folder with the same name in the /saves/ folder.
	 */
	public static void emptyOldWorldProperties()
	{
		File minecraftSavesFolder = new File(RadixCore.getInstance().runningDirectory + "/saves");
		File configSavesFolder = new File(RadixCore.getInstance().runningDirectory + "/config/MCA/Worlds");

		if (!minecraftSavesFolder.exists())
		{
			minecraftSavesFolder.mkdirs();
		}

		if (!configSavesFolder.exists())
		{
			configSavesFolder.mkdirs();
		}

		List<String> minecraftSaves = Arrays.asList(minecraftSavesFolder.list());
		List<String> configSaves = Arrays.asList(configSavesFolder.list());
		List<String> invalidSaves = new ArrayList<String>();

		for (String configSaveName : configSaves)
		{
			if (!minecraftSaves.contains(configSaveName))
			{
				invalidSaves.add(configSaveName);
			}
		}

		for (String invalidSaveName : invalidSaves)
		{
			MCA.getInstance().getLogger().log("Deleted old properties folder: " + invalidSaveName);
			FileSystemOperations.recursiveDeletePath(new File(RadixCore.getInstance().runningDirectory + "/config/MCA/Worlds/" + invalidSaveName));
		}
	}
}

/*******************************************************************************
 * SkinLoader.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import mca.api.registries.VillagerRegistryMCA;
import mca.api.villagers.VillagerEntryMCA;
import mca.core.MCA;

import com.radixshock.radixcore.core.RadixCore;

/**
 * Handles loading of MCA's skins and other addon skins.
 */
public final class SkinLoader 
{
	/**
	 * Atempts to find and load skins from MCA's archive.
	 */
	public static void loadMainSkins()
	{
		try
		{
			final File modFile = findModDataFile();

			if (modFile.isFile())
			{
				loadSkinsFromFile(modFile);
			}

			else
			{
				loadSkinsFromFolder(modFile);
			}
		}

		catch (IOException e)
		{
			RadixCore.getInstance().quitWithException("IOException while loading skins.", e);
		}
	}

	public static void loadAddonSkins()
	{
		MCA.getInstance().getLogger().log("Searching for addons...");

		try
		{
			for (final File fileInMods : new File(RadixCore.getInstance().runningDirectory + "/mods").listFiles())
			{
				if (!fileInMods.getName().equals(findModDataFile().getName()) && fileContainsAddonData(fileInMods))
				{
					loadAddonSkinsFromFile(fileInMods);
				}
			}
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static File findModDataFile() throws ZipException, IOException
	{
		File modData = findModAsArchive();

		if (modData == null)
		{
			modData = findModAsFolder();

			if (modData == null)
			{
				RadixCore.getInstance().quitWithDescription("Unable to find file or folder containing MCA assets.");
			}
		}

		return modData;
	}

	private static File findModAsArchive() throws ZipException, IOException
	{
		final File modsFolder = new File(RadixCore.getInstance().runningDirectory + "/mods");

		for (final File fileInMods : modsFolder.listFiles())
		{
			if (fileInMods.isFile() && fileInMods.getName().contains(".zip"))
			{
				if (fileContainsModData(fileInMods))
				{
					return fileInMods;
				}
			}

			else if (fileInMods.isDirectory())
			{
				final File modData = getModFileFromNestedFolder(fileInMods);

				if (modData != null)
				{
					return modData;
				}
			}
		}

		return null;
	}

	private static File findModAsFolder() throws IOException
	{
		final File modsFolder = new File(RadixCore.getInstance().runningDirectory + "/mods");

		for (final File fileInMods : modsFolder.listFiles())
		{
			if (fileInMods.isDirectory())
			{
				if (folderContainsModData(fileInMods))
				{
					return fileInMods;
				}

				else
				{
					final File modData = getModFolderFromNestedFolder(fileInMods);

					if (modData != null)
					{
						return modData;
					}
				}
			}
		}

		return null;
	}

	private static void loadSkinsFromFile(File modDataFile) throws ZipException, IOException
	{
		final ZipFile modArchive = new ZipFile(modDataFile);
		final Enumeration enumerator = modArchive.entries();

		while (enumerator.hasMoreElements())
		{
			//Loop through each entry within the JAR until the MCA folder is hit.
			final ZipEntry file = (ZipEntry)enumerator.nextElement();
			String archiveFilePath = "/" + file.getName();

			if (archiveFilePath.contains("textures/skins"))
			{
				for (final VillagerEntryMCA entry : VillagerRegistryMCA.getRegisteredVillagersMap().values())
				{
					if (entry.isDefaultTextureUsed())
					{
						entry.addMaleSkin("textures/api/skins/DefaultM1.png");
						entry.addFemaleSkin("textures/api/skins/DefaultF1.png");
					}

					else
					{
						if (archiveFilePath.contains(entry.getTexturesLocation().replace("/assets/mca/", "")) && !archiveFilePath.contains("sleeping"))
						{
							//Fix the file's location in the JAR and determine what type of villager the skin belongs to.
							//Skins are named like [Profession][Gender][ID].png.
							archiveFilePath = archiveFilePath.replace("/assets/mca/", "");

							if (archiveFilePath.contains(entry.getUnlocalizedProfessionName()))
							{
								if (archiveFilePath.replace("textures/skins/" + entry.getUnlocalizedProfessionName(), "").contains("M"))
								{
									entry.addMaleSkin(archiveFilePath);
								}

								else
								{
									entry.addFemaleSkin(archiveFilePath);
								}
							}
						}
					}
				}
			}
		}

		modArchive.close();
	}

	private static void loadSkinsFromFolder(File modFolder)
	{
		for (final VillagerEntryMCA entry : VillagerRegistryMCA.getRegisteredVillagersMap().values())
		{
			if (entry.isDefaultTextureUsed())
			{
				entry.addMaleSkin("textures/api/skins/DefaultM1.png");
				entry.addFemaleSkin("textures/api/skins/DefaultF1.png");
			}

			else
			{
				final String skinsFolderPath = modFolder + entry.getTexturesLocation();
				final File skinsFolder = new File(skinsFolderPath);

				for (final File skinFile : skinsFolder.listFiles())
				{	
					//Fix the file's location in the folder and determine what type of villager the skin belongs to.
					//Skins are named: [Profession][Gender][ID].png.
					final String fileLocation = skinsFolderPath.replace(modFolder.getAbsolutePath() + "/assets/mca/", "") + skinFile.getName();

					if (fileLocation.contains(entry.getUnlocalizedProfessionName()))
					{
						if (fileLocation.replace("textures/skins/" + entry.getUnlocalizedProfessionName(), "").contains("M"))
						{
							entry.addMaleSkin("mca:" + fileLocation);
						}

						else
						{
							entry.addFemaleSkin("mca:" + fileLocation);
						}
					}
				}
			}
		}
	}

	private static void loadAddonSkinsFromFile(File addonDataFile) throws ZipException, IOException
	{
		final ZipFile modArchive = new ZipFile(addonDataFile);
		final Enumeration enumerator = modArchive.entries();

		while (enumerator.hasMoreElements())
		{
			final ZipEntry file = (ZipEntry)enumerator.nextElement();
			String archiveFilePath = "/" + file.getName();

			for (final VillagerEntryMCA entry : VillagerRegistryMCA.getRegisteredVillagersMap().values())
			{
				if (file.getName().contains(entry.getTexturesLocation()) && file.getName().contains(entry.professionName) && file.getName().contains(".png"))
				{
					MCA.getInstance().getLogger().log(entry.modId);
					
					if (file.getName().replace(entry.getTexturesLocation() + "/" + entry.professionName, "").contains("M"))
					{
						entry.addMaleSkin(entry.getModId() + ":" + file.getName().replace("assets/" + entry.getModId() + "/", ""));
					}

					else
					{
						entry.addFemaleSkin(entry.getModId() + ":" + file.getName().replace("assets/" + entry.getModId() + "/", ""));
					}
				}
			}
		}

		modArchive.close();
	}

	private static File getModFileFromNestedFolder(File nestedFolder) throws IOException
	{
		final File[] nestedFiles = nestedFolder.listFiles();

		for (final File file : nestedFiles)
		{
			if (file.isDirectory())
			{
				getModFileFromNestedFolder(file);
			}

			else
			{
				if (fileContainsModData(file))
				{
					return file;
				}
			}
		}

		return null;
	}

	private static File getModFolderFromNestedFolder(File nestedFolder) throws IOException
	{
		final File[] nestedFiles = nestedFolder.listFiles();

		for (final File file : nestedFiles)
		{
			if (file.isDirectory())
			{
				if (folderContainsModData(file))
				{
					return file;
				}

				else
				{
					getModFolderFromNestedFolder(file);
				}
			}
		}

		return null;
	}

	private static boolean fileContainsModData(File fileToTest) throws IOException
	{
		if (fileToTest.getName().contains(".zip"))
		{
			try
			{
				final ZipFile archive = new ZipFile(fileToTest);
				final Enumeration enumerator = archive.entries();
				ZipEntry entry;					

				while (enumerator.hasMoreElements())
				{
					entry = (ZipEntry)enumerator.nextElement();

					//Test for random files unique to MCA.
					if (entry.getName().contains("mca/core/MCA.class") || entry.getName().contains("sleeping/EE1.png"))
					{
						archive.close();
						return true;
					}
				}

				archive.close();
			}

			catch (ZipException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	private static boolean fileContainsAddonData(File fileToTest) throws IOException
	{
		if (fileToTest.getName().contains(".zip") || fileToTest.getName().contains(".jar"))
		{
			try
			{
				final ZipFile archive = new ZipFile(fileToTest);
				final Enumeration enumerator = archive.entries();
				ZipEntry entry;					

				while (enumerator.hasMoreElements())
				{
					entry = (ZipEntry)enumerator.nextElement();

					for (VillagerEntryMCA villagerEntry : VillagerRegistryMCA.getRegisteredVillagersMap().values())
					{
						if (entry.getName().equals(villagerEntry.texturesLocation))
						{
							MCA.getInstance().getLogger().log("Found addon skins in " + fileToTest.getName() + ".");
							
							archive.close();
							return true;
						}
					}
				}

				archive.close();
			}

			catch (ZipException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	private static boolean folderContainsModData(File folderToTest) throws IOException
	{
		final File testFile1 = new File(folderToTest.getAbsolutePath() + "/mca/core/MCA.class");
		final File testFile2 = new File(folderToTest.getAbsolutePath() + "/assets/mca/textures/skins/EE1.png");

		if (testFile1.exists() || testFile2.exists())
		{
			return true;
		}

		else
		{
			return false;
		}
	}
}

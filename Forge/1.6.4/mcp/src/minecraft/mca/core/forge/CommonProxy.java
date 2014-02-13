/*******************************************************************************
 * CommonProxy.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import mca.api.VillagerEntryMCA;
import mca.api.VillagerRegistryMCA;
import mca.core.MCA;
import mca.tileentity.TileEntityTombstone;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * The proxy used server-side.
 */
public class CommonProxy
{
	/**
	 * Registers all rendering information with Forge.
	 */
	public void registerRenderers() 
	{
		//Server-side.
	}

	/**
	 * Registers all tile entities.
	 */
	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityTombstone.class, TileEntityTombstone.class.getSimpleName());
	}

	/**
	 * Registers tick handlers with Forge.
	 */
	public void registerTickHandlers()
	{
		TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
	}

	public void loadSkins()
	{
		try
		{
			final File modFile = findModData();

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
			MCA.getInstance().quitWithException("IOException while loading skins.", e);
		}
	}

	private File findModData() throws ZipException, IOException
	{
		File modData = findModAsArchive();

		if (modData == null)
		{
			modData = findModAsFolder();

			if (modData == null)
			{
				MCA.getInstance().quitWithDescription("Unable to find file or folder containing MCA assets.");
			}
		}

		return modData;
	}

	private File findModAsArchive() throws ZipException, IOException
	{
		MCA.getInstance().log("Attempting to find MCA as an archive in the mods folder...");
		final File modsFolder = new File(MCA.getInstance().runningDirectory + "/mods");

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

	private File findModAsFolder() throws IOException
	{
		MCA.getInstance().log("Attempting to find MCA as a folder in the mods folder...");
		final File modsFolder = new File(MCA.getInstance().runningDirectory + "/mods");

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

	private void loadSkinsFromFile(File modDataFile) throws ZipException, IOException
	{
		MCA.getInstance().log("Loading skins from data file: " + modDataFile.getName() + "...");

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

	private void loadSkinsFromFolder(File modFolder)
	{
		MCA.getInstance().log("Loading skins from data folder: " + modFolder.getName() + "...");

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
							entry.addMaleSkin(fileLocation);
						}

						else
						{
							entry.addFemaleSkin(fileLocation);
						}
					}
				}
			}
		}
	}

	private File getModFileFromNestedFolder(File nestedFolder) throws IOException
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

	private File getModFolderFromNestedFolder(File nestedFolder) throws IOException
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

	private boolean fileContainsModData(File fileToTest) throws IOException
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
						MCA.getInstance().log(" -" + fileToTest.getName() + " <YES>");
						archive.close();
						return true;
					}
				}

				archive.close();
			}

			catch (ZipException e)
			{
				MCA.getInstance().log(" -" + fileToTest.getName() + " <ERR>");
			}

			MCA.getInstance().log(" -" + fileToTest.getName() + " <NOT>");
		}

		return false;
	}

	private boolean folderContainsModData(File folderToTest) throws IOException
	{
		final File testFile1 = new File(folderToTest.getAbsolutePath() + "/mca/core/MCA.class");
		final File testFile2 = new File(folderToTest.getAbsolutePath() + "/assets/mca/textures/skins/EE1.png");

		if (testFile1.exists() || testFile2.exists())
		{
			MCA.getInstance().log(" -" + folderToTest.getName() + " <YES>");
			return true;
		}

		else
		{
			MCA.getInstance().log(" -" + folderToTest.getName() + " <NOT>");
			return false;
		}
	}
}
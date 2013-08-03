/*******************************************************************************
 * CommonProxy.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.io.File;

import mca.core.MCA;
import mca.core.util.DataStore;
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
		//Nothing to do here. Server side.
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

	/**
	 * Loads the skins located in the mod's archive.
	 */
	public void loadSkinsFromArchive()
	{
		//Open the mod archive to look for the skins.
		java.util.zip.ZipFile modArchive = null;
		java.util.Enumeration enumerator = null;

		try
		{
			File[] filesInMods = new File(MCA.instance.runningDirectory + "/mods").listFiles();

			for (File file : filesInMods)
			{
				if (file.getName().contains("MCA ") && file.getName().contains(".zip"))
				{
					modArchive = new java.util.zip.ZipFile(file);
					break;
				}
			}

			if (modArchive != null)
			{
				enumerator = modArchive.entries();
			}
			
			//Same process as below. Happens on a Mac sometimes.
			else
			{
				MCA.instance.log("Mod archive null. Attempting to read in skins as folder.");

				String folderName = MCA.instance.runningDirectory + "/mods";

				for (File file : new File(folderName).listFiles())
				{
					if (file.getName().contains("MCA ") || file.getName().contains("MCA"))
					{
						folderName = folderName + "/" + file.getName();
					}
				}

				if (folderName.contains("MCA"))
				{
					MCA.instance.log("MCA found as folder. Loading skins...");
					loadSkinsFromFolder(folderName);
					return;
				}

				else
				{
					MCA.instance.log("Failed to find MCA as a folder. Directory: " + folderName);
				}
			}
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Unable to open mod archive and search for skins.", e);
		}

		//Happens on Mac. Treat the mod as a folder.
		if (enumerator == null)
		{
			MCA.instance.log("Enumerator null. Attempting to read in skins as folder.");

			String folderName = MCA.instance.runningDirectory + "/mods";

			for (File file : new File(folderName).listFiles())
			{
				if (file.getName().contains("MCA ") || file.getName().contains("MCA"))
				{
					folderName = folderName + "/" + file.getName();
				}
			}

			if (folderName.contains("MCA"))
			{
				MCA.instance.log("MCA found as folder. Loading skins...");
				loadSkinsFromFolder(folderName);
				return;
			}

			else
			{
				MCA.instance.log("Failed to find MCA as a folder. Directory: " + folderName);
			}
		}

		while (enumerator.hasMoreElements())
		{
			//Loop through each entry within the JAR until the MCA folder is hit.
			java.util.zip.ZipEntry file = (java.util.zip.ZipEntry)enumerator.nextElement();
			String fileLocationInArchive = "/" + file.getName();

			if (fileLocationInArchive.contains("/assets/mca/textures/skins/") && fileLocationInArchive.contains("sleeping") == false)
			{
				//Fix the file's location in the JAR and determine what type of villager the skin belongs to.
				//Skins are named like [Profession][Gender][ID].png.
				fileLocationInArchive = fileLocationInArchive.replace("/assets/mca/", "");

				if (fileLocationInArchive.contains("Farmer"))
				{
					//Remove everything but the Gender and ID to properly identify what the gender is.
					if (fileLocationInArchive.replace("textures/skins/Farmer", "").contains("M"))
					{
						DataStore.farmerSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.farmerSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Librarian"))
				{
					if (fileLocationInArchive.replace("textures/skins/Librarian", "").contains("M"))
					{
						DataStore.librarianSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.librarianSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Priest"))
				{
					if (fileLocationInArchive.replace("textures/skins/Priest", "").contains("M"))
					{
						DataStore.priestSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.priestSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Smith"))
				{
					if (fileLocationInArchive.replace("textures/skins/Smith", "").contains("M"))
					{
						DataStore.smithSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.smithSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Butcher"))
				{
					if (fileLocationInArchive.replace("textures/skins/Butcher", "").contains("M"))
					{
						DataStore.butcherSkinsMale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Guard"))
				{
					if (fileLocationInArchive.replace("textures/skins/Guard", "").contains("M"))
					{
						DataStore.guardSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.guardSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Kid"))
				{
					if (fileLocationInArchive.replace("textures/skins/Kid", "").contains("M"))
					{
						DataStore.kidSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.kidSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Baker"))
				{
					if (fileLocationInArchive.replace("textures/skins/Baker", "").contains("M"))
					{
						DataStore.bakerSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.bakerSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Miner"))
				{
					if (fileLocationInArchive.replace("textures/skins/Miner", "").contains("M"))
					{
						DataStore.minerSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						DataStore.minerSkinsFemale.add(fileLocationInArchive);
					}
				}
			}
		}

		//Determine how many skins were loaded for logging purposes.
		int loadedSkins = DataStore.farmerSkinsMale.size() + DataStore.librarianSkinsMale.size() +
				DataStore.priestSkinsMale.size() + DataStore.smithSkinsMale .size() +
				DataStore.butcherSkinsMale.size() + DataStore.guardSkinsMale.size() + 
				DataStore.kidSkinsMale.size() + DataStore.bakerSkinsMale.size() +
				DataStore.minerSkinsMale.size() + DataStore.farmerSkinsFemale.size() +
				DataStore.librarianSkinsFemale.size() + DataStore.priestSkinsFemale.size() +
				DataStore.smithSkinsFemale.size() + DataStore.butcherSkinsFemale.size() +
				DataStore.guardSkinsFemale.size() + DataStore.kidSkinsFemale.size() +
				DataStore.bakerSkinsFemale.size() + DataStore.minerSkinsFemale.size();

		MCA.instance.log("Loaded " + loadedSkins + " skins from mod archive.");
	}

	public void loadSkinsFromFolder(String folderName)
	{
		MCA.instance.log(folderName);
		
		String skinsFolder = folderName + "/assets/mca/textures/skins/";
		String sleepingSkinsFolder = folderName + "/assets/mca/textures/skins/sleeping/";

		for (File fileName : new File(skinsFolder).listFiles())
		{
			//Fix the file's location in the folder and determine what type of villager the skin belongs to.
			//Skins are named like [Profession][Gender][ID].png.
			String fileLocation = skinsFolder.replace(folderName + "/assets/mca/", "") + "/" + fileName.getName();

			//Two slashes. Because why the hell not?
			if (fileLocation.contains("Farmer"))
			{
				//Remove everything but the Gender and ID to properly identify what the gender is.
				if (fileLocation.replace("textures/skins//Farmer", "").contains("M"))
				{
					DataStore.farmerSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.farmerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Librarian"))
			{
				if (fileLocation.replace("textures/skins//Librarian", "").contains("M"))
				{
					DataStore.librarianSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.librarianSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Priest"))
			{
				if (fileLocation.replace("textures/skins//Priest", "").contains("M"))
				{
					DataStore.priestSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.priestSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Smith"))
			{
				if (fileLocation.replace("textures/skins//Smith", "").contains("M"))
				{
					DataStore.smithSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.smithSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Butcher"))
			{
				if (fileLocation.replace("textures/skins//Butcher", "").contains("M"))
				{
					DataStore.butcherSkinsMale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Guard"))
			{
				if (fileLocation.replace("textures/skins//Guard", "").contains("M"))
				{
					DataStore.guardSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.guardSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Kid"))
			{
				if (fileLocation.replace("textures/skins//Kid", "").contains("M"))
				{
					DataStore.kidSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.kidSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Baker"))
			{
				if (fileLocation.replace("textures/skins//Baker", "").contains("M"))
				{
					DataStore.bakerSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.bakerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Miner"))
			{
				if (fileLocation.replace("textures/skins//Miner", "").contains("M"))
				{
					DataStore.minerSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.minerSkinsFemale.add(fileLocation);
				}
			}
		}

		for (File fileName : new File(sleepingSkinsFolder).listFiles())
		{
			//Fix the file's location in the folder and determine what type of villager the skin belongs to.
			//Skins are named like [Profession][Gender][ID].png.
			String fileLocation = skinsFolder.replace(folderName + "/assets/mca/", "") + "/" + fileName.getName();

			if (fileLocation.contains("Farmer"))
			{
				//Remove everything but the Gender and ID to properly identify what the gender is.
				if (fileLocation.replace("textures/skins//Farmer", "").contains("M"))
				{
					DataStore.farmerSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.farmerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Librarian"))
			{
				if (fileLocation.replace("textures/skins//Librarian", "").contains("M"))
				{
					DataStore.librarianSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.librarianSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Priest"))
			{
				if (fileLocation.replace("textures/skins//Priest", "").contains("M"))
				{
					DataStore.priestSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.priestSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Smith"))
			{
				if (fileLocation.replace("textures/skins//Smith", "").contains("M"))
				{
					DataStore.smithSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.smithSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Butcher"))
			{
				if (fileLocation.replace("textures/skins//Butcher", "").contains("M"))
				{
					DataStore.butcherSkinsMale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Guard"))
			{
				if (fileLocation.replace("textures/skins//Guard", "").contains("M"))
				{
					DataStore.guardSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.guardSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Kid"))
			{
				if (fileLocation.replace("textures/skins//Kid", "").contains("M"))
				{
					DataStore.kidSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.kidSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Baker"))
			{
				if (fileLocation.replace("textures/skins//Baker", "").contains("M"))
				{
					DataStore.bakerSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.bakerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Miner"))
			{
				if (fileLocation.replace("textures/skins//Miner", "").contains("M"))
				{
					DataStore.minerSkinsMale.add(fileLocation);
				}

				else
				{
					DataStore.minerSkinsFemale.add(fileLocation);
				}
			}
		}
		
		//Determine how many skins were loaded for logging purposes.
		int loadedSkins = DataStore.farmerSkinsMale.size() + DataStore.librarianSkinsMale.size() +
				DataStore.priestSkinsMale.size() + DataStore.smithSkinsMale .size() +
				DataStore.butcherSkinsMale.size() + DataStore.guardSkinsMale.size() + 
				DataStore.kidSkinsMale.size() + DataStore.bakerSkinsMale.size() +
				DataStore.minerSkinsMale.size() + DataStore.farmerSkinsFemale.size() +
				DataStore.librarianSkinsFemale.size() + DataStore.priestSkinsFemale.size() +
				DataStore.smithSkinsFemale.size() + DataStore.butcherSkinsFemale.size() +
				DataStore.guardSkinsFemale.size() + DataStore.kidSkinsFemale.size() +
				DataStore.bakerSkinsFemale.size() + DataStore.minerSkinsFemale.size();

		MCA.instance.log("Loaded " + loadedSkins + " skins from mod folder.");
		
		if (loadedSkins == 0)
		{
			MCA.instance.quitWithError("Zero skins were loaded. All attempts to read failed.", new Throwable());
		}
	}
}
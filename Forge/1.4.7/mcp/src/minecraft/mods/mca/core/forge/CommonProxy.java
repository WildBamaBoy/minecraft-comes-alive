/*******************************************************************************
 * CommonProxy.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.core.forge;

import java.io.File;

import mods.mca.core.MCA;
import mods.mca.tileentity.TileEntityTombstone;
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
				return;
			}
		}

		while (enumerator.hasMoreElements())
		{
			//Loop through each entry within the JAR until the MCA folder is hit.
			java.util.zip.ZipEntry file = (java.util.zip.ZipEntry)enumerator.nextElement();
			String fileLocationInArchive = "/" + file.getName();

			if (fileLocationInArchive.contains("/mods/mca/textures/skins/") && fileLocationInArchive.contains("sleeping") == false)
			{
				if (fileLocationInArchive.contains("Farmer"))
				{
					//Remove everything but the Gender and ID to properly identify what the gender is.
					if (fileLocationInArchive.replace("textures/skins/Farmer", "").contains("M"))
					{
						MCA.farmerSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.farmerSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Librarian"))
				{
					if (fileLocationInArchive.replace("textures/skins/Librarian", "").contains("M"))
					{
						MCA.librarianSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.librarianSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Priest"))
				{
					if (fileLocationInArchive.replace("textures/skins/Priest", "").contains("M"))
					{
						MCA.priestSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.priestSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Smith"))
				{
					if (fileLocationInArchive.replace("textures/skins/Smith", "").contains("M"))
					{
						MCA.smithSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.smithSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Butcher"))
				{
					if (fileLocationInArchive.replace("textures/skins/Butcher", "").contains("M"))
					{
						MCA.butcherSkinsMale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Guard"))
				{
					if (fileLocationInArchive.replace("textures/skins/Guard", "").contains("M"))
					{
						MCA.guardSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.guardSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Kid"))
				{
					if (fileLocationInArchive.replace("textures/skins/Kid", "").contains("M"))
					{
						MCA.kidSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.kidSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Baker"))
				{
					if (fileLocationInArchive.replace("textures/skins/Baker", "").contains("M"))
					{
						MCA.bakerSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.bakerSkinsFemale.add(fileLocationInArchive);
					}
				}

				else if (fileLocationInArchive.contains("Miner"))
				{
					if (fileLocationInArchive.replace("textures/skins/Miner", "").contains("M"))
					{
						MCA.minerSkinsMale.add(fileLocationInArchive);
					}

					else
					{
						MCA.minerSkinsFemale.add(fileLocationInArchive);
					}
				}
			}
		}

		//Determine how many skins were loaded for logging purposes.
		int loadedSkins = MCA.farmerSkinsMale.size() + MCA.librarianSkinsMale.size() +
				MCA.priestSkinsMale.size() + MCA.smithSkinsMale .size() +
				MCA.butcherSkinsMale.size() + MCA.guardSkinsMale.size() + 
				MCA.kidSkinsMale.size() + MCA.bakerSkinsMale.size() +
				MCA.minerSkinsMale.size() + MCA.farmerSkinsFemale.size() +
				MCA.librarianSkinsFemale.size() + MCA.priestSkinsFemale.size() +
				MCA.smithSkinsFemale.size() + MCA.butcherSkinsFemale.size() +
				MCA.guardSkinsFemale.size() + MCA.kidSkinsFemale.size() +
				MCA.bakerSkinsFemale.size() + MCA.minerSkinsFemale.size();

		MCA.instance.log("Loaded " + loadedSkins + " skins from mod archive.");
	}

	/**
	 * Assumes that the MCA archive has been turned into a folder and attempts to read skins.
	 * 
	 * @param 	folderName	The location of the mods folder.
	 */
	public void loadSkinsFromFolder(String folderName)
	{
		MCA.instance.log(folderName);
		
		String skinsFolder = folderName + "/mods/mca/textures/skins/";
		String sleepingSkinsFolder = folderName + "/mods/mca/textures/skins/sleeping/";

		for (File fileName : new File(skinsFolder).listFiles())
		{
			//Fix the file's location in the folder and determine what type of villager the skin belongs to.
			//Skins are named like [Profession][Gender][ID].png.
			String fileLocation = skinsFolder + "/" + fileName.getName();

			//Two slashes. Because why the hell not?
			if (fileLocation.contains("Farmer"))
			{
				//Remove everything but the Gender and ID to properly identify what the gender is.
				if (fileLocation.replace("textures/skins//Farmer", "").contains("M"))
				{
					MCA.farmerSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.farmerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Librarian"))
			{
				if (fileLocation.replace("textures/skins//Librarian", "").contains("M"))
				{
					MCA.librarianSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.librarianSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Priest"))
			{
				if (fileLocation.replace("textures/skins//Priest", "").contains("M"))
				{
					MCA.priestSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.priestSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Smith"))
			{
				if (fileLocation.replace("textures/skins//Smith", "").contains("M"))
				{
					MCA.smithSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.smithSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Butcher"))
			{
				if (fileLocation.replace("textures/skins//Butcher", "").contains("M"))
				{
					MCA.butcherSkinsMale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Guard"))
			{
				if (fileLocation.replace("textures/skins//Guard", "").contains("M"))
				{
					MCA.guardSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.guardSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Kid"))
			{
				if (fileLocation.replace("textures/skins//Kid", "").contains("M"))
				{
					MCA.kidSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.kidSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Baker"))
			{
				if (fileLocation.replace("textures/skins//Baker", "").contains("M"))
				{
					MCA.bakerSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.bakerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Miner"))
			{
				if (fileLocation.replace("textures/skins//Miner", "").contains("M"))
				{
					MCA.minerSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.minerSkinsFemale.add(fileLocation);
				}
			}
		}

		for (File fileName : new File(sleepingSkinsFolder).listFiles())
		{
			//Fix the file's location in the folder and determine what type of villager the skin belongs to.
			//Skins are named like [Profession][Gender][ID].png.
			String fileLocation = sleepingSkinsFolder + "/" + fileName.getName();

			if (fileLocation.contains("Farmer"))
			{
				//Remove everything but the Gender and ID to properly identify what the gender is.
				if (fileLocation.replace("textures/skins//Farmer", "").contains("M"))
				{
					MCA.farmerSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.farmerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Librarian"))
			{
				if (fileLocation.replace("textures/skins//Librarian", "").contains("M"))
				{
					MCA.librarianSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.librarianSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Priest"))
			{
				if (fileLocation.replace("textures/skins//Priest", "").contains("M"))
				{
					MCA.priestSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.priestSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Smith"))
			{
				if (fileLocation.replace("textures/skins//Smith", "").contains("M"))
				{
					MCA.smithSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.smithSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Butcher"))
			{
				if (fileLocation.replace("textures/skins//Butcher", "").contains("M"))
				{
					MCA.butcherSkinsMale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Guard"))
			{
				if (fileLocation.replace("textures/skins//Guard", "").contains("M"))
				{
					MCA.guardSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.guardSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Kid"))
			{
				if (fileLocation.replace("textures/skins//Kid", "").contains("M"))
				{
					MCA.kidSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.kidSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Baker"))
			{
				if (fileLocation.replace("textures/skins//Baker", "").contains("M"))
				{
					MCA.bakerSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.bakerSkinsFemale.add(fileLocation);
				}
			}

			else if (fileLocation.contains("Miner"))
			{
				if (fileLocation.replace("textures/skins//Miner", "").contains("M"))
				{
					MCA.minerSkinsMale.add(fileLocation);
				}

				else
				{
					MCA.minerSkinsFemale.add(fileLocation);
				}
			}
		}
		
		//Determine how many skins were loaded for logging purposes.
		int loadedSkins = MCA.farmerSkinsMale.size() + MCA.librarianSkinsMale.size() +
				MCA.priestSkinsMale.size() + MCA.smithSkinsMale .size() +
				MCA.butcherSkinsMale.size() + MCA.guardSkinsMale.size() + 
				MCA.kidSkinsMale.size() + MCA.bakerSkinsMale.size() +
				MCA.minerSkinsMale.size() + MCA.farmerSkinsFemale.size() +
				MCA.librarianSkinsFemale.size() + MCA.priestSkinsFemale.size() +
				MCA.smithSkinsFemale.size() + MCA.butcherSkinsFemale.size() +
				MCA.guardSkinsFemale.size() + MCA.kidSkinsFemale.size() +
				MCA.bakerSkinsFemale.size() + MCA.minerSkinsFemale.size();

		MCA.instance.log("Loaded " + loadedSkins + " skins from mod folder.");
		
		if (loadedSkins == 0)
		{
			MCA.instance.quitWithError("Zero skins were loaded. All attempts to read failed.", new Throwable());
		}
	}
}
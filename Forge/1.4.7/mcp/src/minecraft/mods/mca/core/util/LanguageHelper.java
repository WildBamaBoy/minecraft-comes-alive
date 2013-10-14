/*******************************************************************************
 * LanguageHelper.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.core.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import mods.mca.core.MCA;
import mods.mca.core.io.WorldPropertiesManager;
import mods.mca.core.util.object.UpdateHandler;
import mods.mca.entity.AbstractEntity;
import mods.mca.entity.EntityChild;
import mods.mca.entity.EntityPlayerChild;
import mods.mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringTranslate;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * Handles loading the language files into the mod and retrieving strings from them.
 */
public final class LanguageHelper 
{
	/** Map containing language IDs and their names in English. */
	private static final Map<String, String> languagesMap = new HashMap<String, String>();

	/** The English name for the language. */
	private static String languageName = "";

	/** The properties instance used to load languages. */
	private static Properties properties = new Properties();
	
	/**
	 * Loads the language whose ID is in the options.txt file.
	 */
	public static void loadLanguage()
	{
		loadLanguage(getLanguageIDFromOptions());
		MCA.instance.languageLoaded = true;
	}

	/**
	 * Loads the language with the specified language ID.
	 * 
	 * @param 	languageID	The ID of the language to load.
	 */
	public static void loadLanguage(String languageID)
	{
		//Clear old data.
		MCA.stringTranslations.clear();

		//Get the name and location of the appropriate language file.
		languageName = languagesMap.get(getLanguageIDFromOptions());
		MCA.instance.log("Loading " + languageName + "...");

		try
		{
			properties.load(StringTranslate.class.getResourceAsStream("/mods/mca/language/" + languageName + ".properties"));

			//Loop through each item in the properties instance.
			for (Map.Entry<Object, Object> entrySet : properties.entrySet())
			{
				//OMIT will make the language loader skip that phrase.
				if (!entrySet.getValue().toString().toUpperCase().equals("OMIT"))
				{
					if (entrySet.getKey().toString().contains("name.male"))
					{
						MCA.maleNames.add(entrySet.getValue().toString());
					}

					else if (entrySet.getKey().toString().contains("name.female"))
					{
						MCA.femaleNames.add(entrySet.getValue().toString());
					}

					else
					{
						MCA.stringTranslations.put(entrySet.getKey().toString(), entrySet.getValue().toString());
					}
				}
			}

			//Clear the properties instance.
			properties.clear();
			
			//Add localized item names.
			LanguageRegistry.addName(MCA.instance.itemEngagementRing, LanguageHelper.getString("item.ring.engagement"));
			LanguageRegistry.addName(MCA.instance.itemWeddingRing, LanguageHelper.getString("item.ring.wedding"));
			LanguageRegistry.addName(MCA.instance.itemArrangersRing, LanguageHelper.getString("item.ring.arranger"));
			LanguageRegistry.addName(MCA.instance.itemTombstone, LanguageHelper.getString("item.tombstone"));
			LanguageRegistry.addName(MCA.instance.itemWhistle, LanguageHelper.getString("item.whistle"));
			LanguageRegistry.addName(MCA.instance.itemBabyBoy, LanguageHelper.getString("item.baby.boy"));
			LanguageRegistry.addName(MCA.instance.itemBabyGirl, LanguageHelper.getString("item.baby.girl"));
			LanguageRegistry.addName(MCA.instance.itemEggMale, LanguageHelper.getString("item.egg.male"));
			LanguageRegistry.addName(MCA.instance.itemEggFemale, LanguageHelper.getString("item.egg.female"));
			LanguageRegistry.addName(MCA.instance.itemVillagerEditor, LanguageHelper.getString("item.editor"));
			LanguageRegistry.addName(MCA.instance.itemLostRelativeDocument, LanguageHelper.getString("item.lostrelativedocument"));
			LanguageRegistry.addName(MCA.instance.itemCrown, LanguageHelper.getString("item.crown"));
			LanguageRegistry.addName(MCA.instance.itemHeirCrown, LanguageHelper.getString("item.heircrown"));
			LanguageRegistry.addName(MCA.instance.itemKingsCoat, LanguageHelper.getString("item.kingscoat"));
			LanguageRegistry.addName(MCA.instance.itemKingsPants, LanguageHelper.getString("item.kingspants"));
			LanguageRegistry.addName(MCA.instance.itemKingsBoots, LanguageHelper.getString("item.kingsboots"));
			
			//Add achievements.
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_Charmer", languageID, LanguageHelper.getString("achievement.title.charmer"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_Charmer.desc", languageID, LanguageHelper.getString("achievement.descr.charmer")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_GetMarried", languageID, LanguageHelper.getString("achievement.title.getmarried"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_GetMarried.desc", languageID, LanguageHelper.getString("achievement.descr.getmarried")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveBabyBoy", languageID, LanguageHelper.getString("achievement.title.havebabyboy"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveBabyBoy.desc", languageID, LanguageHelper.getString("achievement.descr.havebabyboy")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveBabyGirl", languageID, LanguageHelper.getString("achievement.title.havebabygirl"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveBabyGirl.desc", languageID, LanguageHelper.getString("achievement.descr.havebabygirl")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_CookBaby", languageID, LanguageHelper.getString("achievement.title.cookbaby"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_CookBaby.desc", languageID, LanguageHelper.getString("achievement.descr.cookbaby")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_BabyGrowUp", languageID, LanguageHelper.getString("achievement.title.growbaby"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_BabyGrowUp.desc", languageID, LanguageHelper.getString("achievement.descr.growbaby")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildFarm", languageID, LanguageHelper.getString("achievement.title.farming"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildFarm.desc", languageID, LanguageHelper.getString("achievement.descr.farming")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildFish", languageID, LanguageHelper.getString("achievement.title.fishing"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildFish.desc", languageID, LanguageHelper.getString("achievement.descr.fishing")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildWoodcut", languageID, LanguageHelper.getString("achievement.title.woodcutting"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildWoodcut.desc", languageID, LanguageHelper.getString("achievement.descr.woodcutting")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildMine", languageID, LanguageHelper.getString("achievement.title.mining"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildMine.desc", languageID, LanguageHelper.getString("achievement.descr.mining")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildHuntKill", languageID, LanguageHelper.getString("achievement.title.huntkill"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildHuntKill.desc", languageID, LanguageHelper.getString("achievement.descr.huntkill"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildHuntTame", languageID, LanguageHelper.getString("achievement.title.hunttame"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildHuntTame.desc", languageID, LanguageHelper.getString("achievement.descr.hunttame"));		
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildGrowUp", languageID, LanguageHelper.getString("achievement.title.growkid"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ChildGrowUp.desc", languageID, LanguageHelper.getString("achievement.descr.growkid")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_AdultFullyEquipped", languageID, LanguageHelper.getString("achievement.title.equipadult"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_AdultFullyEquipped.desc", languageID, LanguageHelper.getString("achievement.descr.equipadult")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_AdultKills", languageID, LanguageHelper.getString("achievement.title.mobkills"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_AdultKills.desc", languageID, LanguageHelper.getString("achievement.descr.mobkills")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_AdultMarried", languageID, LanguageHelper.getString("achievement.title.marrychild"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_AdultMarried.desc", languageID, LanguageHelper.getString("achievement.descr.marrychild")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGrandchild", languageID, LanguageHelper.getString("achievement.title.havegrandchild"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGrandchild.desc", languageID, LanguageHelper.getString("achievement.descr.havegrandchild"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGreatGrandchild", languageID, LanguageHelper.getString("achievement.title.havegreatgrandchild"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGreatGrandchild.desc", languageID, LanguageHelper.getString("achievement.descr.havegreatgrandchild")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGreatx2Grandchild", languageID, LanguageHelper.getString("achievement.title.havegreatx2grandchild"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGreatx2Grandchild.desc", languageID, LanguageHelper.getString("achievement.descr.havegreatx2grandchild")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGreatx10Grandchild", languageID, LanguageHelper.getString("achievement.title.havegreatx10grandchild"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HaveGreatx10Grandchild.desc", languageID, LanguageHelper.getString("achievement.descr.havegreatx10grandchild")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HardcoreSecret", languageID, LanguageHelper.getString("achievement.title.hardcoresecret"));
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_HardcoreSecret.desc", languageID, LanguageHelper.getString("achievement.descr.hardcoresecret")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_CraftCrown", languageID, LanguageHelper.getString("achievement.title.craftcrown")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_CraftCrown.desc", languageID, LanguageHelper.getString("achievement.descr.craftcrown")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ExecuteVillager", languageID, LanguageHelper.getString("achievement.title.executevillager")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_ExecuteVillager.desc", languageID, LanguageHelper.getString("achievement.descr.executevillager")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_MakeKnight", languageID, LanguageHelper.getString("achievement.title.makeknight")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_MakeKnight.desc", languageID, LanguageHelper.getString("achievement.descr.makeknight")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_KnightArmy", languageID, LanguageHelper.getString("achievement.title.knightarmy")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_KnightArmy.desc", languageID, LanguageHelper.getString("achievement.descr.knightarmy")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_MakePeasant", languageID, LanguageHelper.getString("achievement.title.makepeasant")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_MakePeasant.desc", languageID, LanguageHelper.getString("achievement.descr.makepeasant")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_PeasantArmy", languageID, LanguageHelper.getString("achievement.title.peasantarmy")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_PeasantArmy.desc", languageID, LanguageHelper.getString("achievement.descr.peasantarmy")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_NameHeir", languageID, LanguageHelper.getString("achievement.title.nameheir")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_NameHeir.desc", languageID, LanguageHelper.getString("achievement.descr.nameheir")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_MonarchSecret", languageID, LanguageHelper.getString("achievement.title.monarchsecret")); 
			LanguageRegistry.instance().addStringLocalization("achievement." + "MCA_MonarchSecret.desc", languageID, LanguageHelper.getString("achievement.descr.monarchsecret")); 

			LanguageRegistry.reloadLanguageTable();

			MCA.instance.log("Loaded " + MCA.stringTranslations.size() + " phrases in " + languageName + ".");
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithThrowable("Error while loading language.", e);
		}
	}

	/**
	 * Retrieves the specified string from the string translations map. Used when the string being retrieved
	 * is not being spoken by an entity, such as a GUI button or item name.
	 * 
	 * @param	id	The ID of the string to retrieve.
	 * 
	 * @return	Returns localized string matching the ID provided.
	 */
	public static String getString(String id)
	{
		return getString(null, null, id, false, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param 	entity	The MCA entity that is speaking.
	 * @param 	id		The ID of the string to retrieve.
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(AbstractEntity entity, String id)
	{
		return getString(null, entity, id, true, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param 	entity				The MCA entity that is speaking.
	 * @param 	id					The ID of the string to retrieve.
	 * @param	useCharacterType	Should the entity's character type be inserted before the ID of the string?
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(AbstractEntity entity, String id, boolean useCharacterType)
	{
		return getString(null, entity, id, useCharacterType, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param	player				The player that will be receiving this string.
	 * @param 	id					The ID of the string to retrieve.
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(EntityPlayer player, String id)
	{
		return getString(player, null, id, false, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param	player				The player that will be receiving this string.
	 * @param 	entity				The MCA entity that is speaking.
	 * @param 	id					The ID of the string to retrieve.
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(EntityPlayer player, AbstractEntity entity, String id)
	{
		return getString(player, entity, id, true, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param	player				The player that will be receiving this string.
	 * @param 	entity				The MCA entity that is speaking.
	 * @param 	id					The ID of the string to retrieve.
	 * @param	useCharacterType	Should the entity's character type be inserted before the ID of the string?
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(EntityPlayer player, AbstractEntity entity, String id, boolean useCharacterType)
	{
		return getString(player, entity, id, useCharacterType, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param	player				The player that will be receiving this string.
	 * @param 	entity				The MCA entity that is speaking.
	 * @param 	id					The ID of the string to retrieve.
	 * @param	useCharacterType	Should the entity's character type be inserted before the ID of the string?
	 * @param	prefix				The string that should be added to the beginning of the localized string.
	 * @param	suffix				The string that should be added to the end of the localized string.
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(EntityPlayer player, AbstractEntity entity, String id, boolean useCharacterType, String prefix, String suffix)
	{
		if (MinecraftServer.getServer() != null)
		{
			//Call to getString on server. Invalid as the player will receive an untranslated string.
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && MCA.instance.isDedicatedServer)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHelper.createSayLocalizedPacket(player, entity, id, useCharacterType, prefix, suffix), (Player)player);
				return "";
			}
		}

		int playerId = MCA.instance.getIdOfPlayer(player);

		if (useCharacterType)
		{
			id = entity.getCharacterType(playerId) + "." + id;
		}

		List<String> matchingValuesList = new ArrayList();

		//Loop through each item in the string translations map.
		for (Map.Entry<String, String> entrySet : MCA.stringTranslations.entrySet())
		{
			//Check if the entry's key contains the ID.
			if (entrySet.getKey().contains(id))
			{
				//Then check if it completely equals the ID.
				if (entrySet.getKey().equals(id))
				{
					//In this case, clear the values list and add only the value that equals the ID.
					matchingValuesList.clear();
					matchingValuesList.add(entrySet.getValue());
					break;
				}

				else //Otherwise just add the matching ID's value to the matching values list.
				{
					matchingValuesList.add(entrySet.getValue());
				}
			}
		}

		if (matchingValuesList.size() == 0)
		{
			new Throwable().printStackTrace();
			return "(" + id + " not found)";
		}

		else
		{
			//Find an instance of Random rather than making a new one.
			Random rand = null;
			
			if (player != null)
			{
				rand = player.worldObj.rand;
			}
			
			else if (entity != null)
			{
				rand = entity.worldObj.rand;
			}
			
			else
			{
				rand = new Random();
			}
			
			if (prefix != null && suffix != null)
			{
				return prefix + parseString(player, entity, matchingValuesList.get(rand.nextInt(matchingValuesList.size())) + suffix);
			}

			else if (prefix != null)
			{
				return prefix + parseString(player, entity, matchingValuesList.get(rand.nextInt(matchingValuesList.size())));
			}

			else if (suffix != null)
			{
				return parseString(player, entity, matchingValuesList.get(rand.nextInt(matchingValuesList.size())) + suffix);
			}

			else
			{
				return parseString(player, entity, matchingValuesList.get(rand.nextInt(matchingValuesList.size())));
			}
		}
	}

	/**
	 * Parses the variables within the specified text.
	 * 
	 * @param	player	The player whose properties to use for parsing.
	 * @param 	entity	The entity that is speaking.
	 * @param 	text	The text to parse.
	 * 
	 * @return	Returns string with all variables replaced with their appropriate information.
	 */
	private static String parseString(EntityPlayer player, AbstractEntity entity, String text)
	{
		int playerId = 0;
		WorldPropertiesManager worldPropertiesManager = null;

		if (player != null)
		{
			worldPropertiesManager = MCA.instance.playerWorldManagerMap.get(player.username);
			playerId = MCA.instance.getIdOfPlayer(player);
		}

		try
		{
			if (text.contains("%Name%"))
			{
				text = text.replace("%Name%", entity.name);
			}

			if (text.contains("%RelationToPlayer%"))
			{
				text = text.replace("%RelationToPlayer%", entity.familyTree.getRelationTo(playerId).toString());
			}

			if (text.contains("%RelationOfPlayer%"))
			{
				text = text.replace("%RelationOfPlayer%", entity.familyTree.getRelationOf(playerId).toString());
			}

			if (text.contains("%MotherName%"))
			{
				if (entity instanceof EntityPlayerChild)
				{
					List<Integer> parents = entity.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

					if (parents.get(0) < 0 && parents.get(1) < 0)
					{
						text = text.replace("%MotherName%", player.username);
					}

					//One of the parents is not a player (since this is a player child no further logic is required.)
					//Always use the player's name as the first name.
					else
					{
						text = text.replace("%MotherName%", ((EntityPlayerChild)entity).ownerPlayerName);
					}
				}

				else
				{
					try
					{
						List<Integer> parents = entity.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

						AbstractEntity parent1 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(0)));
						AbstractEntity parent2 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(1)));

						if (parent1.gender.equals(parent2.gender))
						{
							text = text.replace("%MotherName%", parent1.name);
						}

						else if (parent1.gender.equals("Male"))
						{
							text = text.replace("%MotherName%", parent2.name);
						}

						else
						{
							text = text.replace("%MotherName%", parent1.name);
						}
					}

					catch (NullPointerException e)
					{
						text = LanguageHelper.getString("gui.info.family.parents.deceased");
					}
				}
			}

			if (text.contains("%FatherName%"))
			{
				if (entity instanceof EntityPlayerChild)
				{
					List<Integer> parents = entity.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

					if (parents.get(0) < 0 && parents.get(1) < 0)
					{
						text = text.replace("%FatherName%", MCA.instance.playerWorldManagerMap.get(player.username).worldProperties.playerSpouseName);
					}

					//One of the parents is not a player (since this is a player child no further logic is required.)
					//Always use the villager as the last name.
					else
					{
						try
						{
							AbstractEntity parent = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(1)));
							text = text.replace("%FatherName%", parent.getTitle(0, false));
						}

						catch (NullPointerException e)
						{
							AbstractEntity parent = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(0)));
							text = text.replace("%FatherName%", parent.getTitle(0, false));
						}
					}
				}

				else
				{
					try
					{
						List<Integer> parents = entity.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

						AbstractEntity parent1 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(0)));
						AbstractEntity parent2 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(1)));

						if (parent1.gender.equals(parent2.gender))
						{
							text = text.replace("%FatherName%", parent2.name);
						}

						else if (parent1.gender.equals("Male"))
						{
							text = text.replace("%FatherName%", parent1.name);
						}

						else
						{
							text = text.replace("%FatherName%", parent2.name);
						}
					}

					catch (NullPointerException e)
					{
						text = LanguageHelper.getString("gui.info.family.parents.deceased");
					}
				}
			}

			if (text.contains("%SpouseRelation%"))
			{
				AbstractEntity spouse = entity.familyTree.getInstanceOfRelative(EnumRelation.Spouse);
				text = text.replace("%SpouseRelation%", spouse.familyTree.getRelationTo(playerId).toString(spouse, spouse.gender, true));
			}

			if (text.contains("%PlayerSpouseName%"))
			{
				//Check world properties to see if the player is married to another player or an NPC.
				if (worldPropertiesManager.worldProperties.playerSpouseID > 0)
				{
					//Player married to NPC, so the NPC is provided.
					text = text.replace("%PlayerSpouseName%", entity.familyTree.getInstanceOfRelative(EnumRelation.Spouse).name);
				}

				else
				{
					text = text.replace("%PlayerSpouseName%", worldPropertiesManager.worldProperties.playerSpouseName);
				}
			}
			
			if (text.contains("%VillagerSpouseName%"))
			{
				AbstractEntity spouse = entity.familyTree.getInstanceOfRelative(EnumRelation.Spouse);
				
				if (spouse != null)
				{
					text = text.replace("%VillagerSpouseName%", spouse.name);
				}
			}

			if (text.contains("%SpouseFullName%"))
			{
				AbstractEntity spouse = entity.familyTree.getInstanceOfRelative(EnumRelation.Spouse);

				if (spouse != null)
				{
					text = text.replace("%SpouseFullName%", entity.familyTree.getInstanceOfRelative(EnumRelation.Spouse).getTitle(playerId, true));
				}

				else
				{
					//Must be a player if spouse is null. Use the value of that field.
					text = text.replace("%SpouseFullName%", entity.spousePlayerName);
				}
			}

			if (text.contains("%Generation%"))
			{
				text = text.replace("%Generation%", String.valueOf(entity.generation));
			}

			if (text.contains("%OreType%"))
			{
				EntityChild child = (EntityChild)entity;

				String oreName = "";

				switch (child.miningChore.oreType)
				{
				case 0: oreName = "Coal"; break;
				case 1: oreName = "Steel"; break;
				case 2: oreName = "Lapis"; break;
				case 3: oreName = "Gold"; break;
				case 4: oreName = "Diamond"; break;
				case 5: oreName = "Redstone"; break;
				case 6: oreName = "Emerald"; break;
				}

				text = text.replace("%OreType%", LanguageHelper.getString("gui.button.chore.mining.find." + oreName.toLowerCase()).toLowerCase());
			}

			if (text.contains("%OreDistance%"))
			{
				EntityChild child = (EntityChild)entity;
				text = text.replace("%OreDistance%", String.valueOf(child.miningChore.passiveDistanceToOre));
			}

			if (text.contains("%ChildTitle%"))
			{
				if (entity.gender.equals("Male"))
				{
					text = text.replace("%ChildTitle%", LanguageHelper.getString("family.son"));
				}

				else
				{
					text = text.replace("%ChildTitle%", LanguageHelper.getString("family.daughter"));
				}
			}

			if (text.contains("%RandomName%"))
			{
				if (entity.gender.equals("Male"))
				{
					text = text.replace("%RandomName%", AbstractEntity.getRandomName("Female"));
				}

				else
				{
					text = text.replace("%RandomName%", AbstractEntity.getRandomName("Male"));
				}
			}

			if (text.contains("%CallPlayerParent%"))
			{
				if (!MCA.instance.isDedicatedServer)
				{
					if (worldPropertiesManager.worldProperties.playerGender.equals("Male"))
					{
						text = text.replace("%CallPlayerParent%", getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.male"));
					}

					else if (worldPropertiesManager.worldProperties.playerGender.equals("Female"))
					{
						text = text.replace("%CallPlayerParent%", getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.female"));
					}
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.instance.playerWorldManagerMap.get(player.username);

					if (serverPropertiesManager.worldProperties.playerGender.equals("Male"))
					{
						text = text.replace("%CallPlayerParent%", getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.male"));
					}

					else if (serverPropertiesManager.worldProperties.playerGender.equals("Female"))
					{
						text = text.replace("%CallPlayerParent%", getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.female"));
					}
				}
			}

			if (text.contains("%PlayerName%"))
			{
				if (!MCA.instance.isDedicatedServer)
				{
					text = text.replace("%PlayerName%", worldPropertiesManager.worldProperties.playerName);
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.instance.playerWorldManagerMap.get(player.username);
					text = text.replace("%PlayerName%", serverPropertiesManager.worldProperties.playerName);
				}
			}

			if (text.contains("%TruePlayerName%"))
			{
				text = text.replace("%TruePlayerName%", player.username);
			}

			if (text.contains("%ParentOpposite%"))
			{
				if (!MCA.instance.isDedicatedServer)
				{
					if (worldPropertiesManager.worldProperties.playerGender.equals("Male"))
					{
						text = text.replace("%ParentOpposite%", getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.male"));
					}

					else if (worldPropertiesManager.worldProperties.playerGender.equals("Female"))
					{
						text = text.replace("%ParentOpposite%", getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.female"));
					}
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.instance.playerWorldManagerMap.get(player.username);

					if (serverPropertiesManager.worldProperties.playerGender.equals("Male"))
					{
						text = text.replace("%ParentOpposite%", getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.male"));
					}

					else if (serverPropertiesManager.worldProperties.playerGender.equals("Female"))
					{
						text = text.replace("%ParentOpposite%", getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.female"));
					}
				}
			}

			if (text.contains("%BabyName%"))
			{
				if (!MCA.instance.isDedicatedServer)
				{
					text = text.replace("%BabyName%", worldPropertiesManager.worldProperties.babyName);
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.instance.playerWorldManagerMap.get(player.username);
					text = text.replace("%BabyName%", serverPropertiesManager.worldProperties.babyName);
				}
			}
			
			if (text.contains("%MonarchTitle%"))
			{
				if (worldPropertiesManager.worldProperties.playerGender.equals("Male"))
				{
					text = text.replace("%MonarchTitle%", getString("monarch.title.male.player"));
				}
				
				else
				{
					text = text.replace("%MonarchTitle%", getString("monarch.title.female.player"));
				}
			}
			
			if (text.contains("%MonarchPlayerName%"))
			{
				text = text.replace("%MonarchPlayerName%", entity.monarchPlayerName);
			}
			
			if (text.contains("%Trait%"))
			{
				text = text.replace("%Trait%", entity.trait.getLocalizedValue());
			}
			
			if (text.contains("%MinecraftVersionNumber%"))
			{
				text = text.replace("%MinecraftVersionNumber%", UpdateHandler.compatibleMinecraftVersion);
			}
			
			if (text.contains("%ModVersionNumber%"))
			{
				text = text.replace("%ModVersionNumber%", UpdateHandler.mostRecentVersion);
			}
			
			if (text.contains("%URL%"))
			{
				text = text.replace("%URL%", Color.BLUE + Format.ITALIC + "http://goo.gl/4Kwohv" + Format.RESET + Color.YELLOW);
			}
			
			if (text.contains("%LivingParent%"))
			{
				List<Integer> parents = entity.familyTree.getEntitiesWithRelation(EnumRelation.Parent);
				AbstractEntity parent1 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(0)));
				AbstractEntity parent2 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.instance.idsMap.get(parents.get(1)));
				
				AbstractEntity nonNullParent = parent1 != null ? parent1 : parent2 != null ? parent2 : null;

				text = text.replace("%LivingParent%", nonNullParent.name);
			}
		}

		catch (NullPointerException e)
		{
			MCA.instance.log(e);
			text += " (Parsing error)";
		}

		return text;
	}

	/**
	 * Reads Minecraft's options file and retrieves the language ID from it.
	 * 
	 * @return	Returns the language ID last loaded by Minecraft.
	 */
	public static String getLanguageIDFromOptions()
	{
		BufferedReader reader = null;

		try 
		{
			reader = new BufferedReader(new FileReader(MCA.instance.runningDirectory + "/options.txt"));

			String line = "";

			while (line != null)
			{
				line = reader.readLine();

				if (line.contains("lang:"))
				{
					break;
				}
			}

			if (!line.isEmpty())
			{
				reader.close();
				return line.substring(5);
			}
		} 

		catch (FileNotFoundException e) 
		{
			MCA.instance.log("Could not find options.txt file. Defaulting to English.");
			return "en_US";
		} 

		catch (IOException e)
		{
			MCA.instance.quitWithThrowable("Error reading from Minecraft options.txt file.", e);
			return null;
		}

		catch (NullPointerException e)
		{
			MCA.instance.log("NullPointerException while trying to read options.txt. Defaulting to English.");
			return "en_US";
		}

		return null;
	}

	static
	{
		languagesMap.put("af_ZA", "Afrikaans");
		languagesMap.put("ar_SA", "Arabic");
		languagesMap.put("bg_BG", "Bulgarian");
		languagesMap.put("ca_ES", "Catalan");
		languagesMap.put("cs_CZ", "Czech");
		languagesMap.put("cy_GB", "Welsh");
		languagesMap.put("da_DK", "Danish");
		languagesMap.put("de_DE", "German");
		languagesMap.put("el_GR", "Greek");
		languagesMap.put("en_AU", "English");
		languagesMap.put("en_CA", "English");
		languagesMap.put("en_GB", "English");
		languagesMap.put("en_PT", "Pirate");
		languagesMap.put("en_US", "English");
		languagesMap.put("eo_UY", "Esperanto");
		languagesMap.put("es_AR", "Argentina Spanish");
		languagesMap.put("es_ES", "Spanish");
		languagesMap.put("es_MX", "Mexico Spanish");
		languagesMap.put("es_UY", "Uruguay Spanish");
		languagesMap.put("es_VE", "Venezuela Spanish");
		languagesMap.put("et_EE", "Estonian");
		languagesMap.put("eu_ES", "Basque");
		languagesMap.put("fi_FI", "Finnish");
		languagesMap.put("fr_FR", "French");
		languagesMap.put("fr_CA", "Canadian French");
		languagesMap.put("ga_IE", "Irish");
		languagesMap.put("gl_ES", "Galician");
		languagesMap.put("he_IL", "Hebrew");
		languagesMap.put("hi_IN", "Hindi");
		languagesMap.put("hr_HR", "Croatian");
		languagesMap.put("hu_HU", "Hungarian");
		languagesMap.put("id_ID", "Bahasa Indonesia");
		languagesMap.put("is_IS", "Icelandic");
		languagesMap.put("it_IT", "Italian");
		languagesMap.put("ja_JP", "Japanese");
		languagesMap.put("ka_GE", "Georgian");
		languagesMap.put("ko_KR", "Korean");
		languagesMap.put("ko_KO", "Cornish");
		languagesMap.put("lt_LT", "Lithuanian");
		languagesMap.put("lv_LV", "Latvian");
		languagesMap.put("ms_MY", "Malay");
		languagesMap.put("mt_MT", "Maltese");
		languagesMap.put("nl_NL", "Dutch");
		languagesMap.put("nn_NO", "Nynorsk");
		languagesMap.put("nb_NO", "Norwegian");
		languagesMap.put("pl_PL", "Polish");
		languagesMap.put("pt_BR", "Brazilian Portuguese");
		languagesMap.put("pt_PT", "Portuguese");
		languagesMap.put("qya_AA", "Quenya");
		languagesMap.put("ru_RU", "Russian");
		languagesMap.put("sk_SK", "Slovak");
		languagesMap.put("sl_SI", "Slovenian");
		languagesMap.put("sr_SP", "Serbian");
		languagesMap.put("sv_SE", "Swedish");
		languagesMap.put("th_TH", "Thai");
		languagesMap.put("tlh_AA", "Klingon");
		languagesMap.put("tr_TR", "Turkish");
		languagesMap.put("uk_UA", "Ukrainian");
		languagesMap.put("vi_VN", "Vietnamese");
		languagesMap.put("zh_CN", "Chinese Simplified");
		languagesMap.put("zh_TW", "Chinese Traditional");
	}
}

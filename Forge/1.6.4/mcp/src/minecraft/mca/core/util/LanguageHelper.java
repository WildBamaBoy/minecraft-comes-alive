/*******************************************************************************
 * LanguageHelper.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.object.UpdateHandler;
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringTranslate;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * Handles loading the language files into the mod and retrieving strings from them.
 */
public final class LanguageHelper 
{
	/** Map containing language IDs and their names in English. */
	private static final ConcurrentHashMap<String, String> LANGUAGE_MAP = new ConcurrentHashMap<String, String>();

	/** Map that contains string translations loaded from language files. */
	private static ConcurrentHashMap<String, String> translationsMap = new ConcurrentHashMap();

	/** The English name for the language. */
	private static String languageName = "";

	/** The properties instance used to load languages. */
	private static Properties properties = new Properties();

	/** Private constructor. */
	private LanguageHelper() {}

	/**
	 * Loads the language whose ID is in the options.txt file.
	 */
	public static void loadLanguage()
	{
		loadLanguage(getLanguageIDFromOptions());
		MCA.getInstance().languageLoaded = true;
	}

	/**
	 * Loads the language with the specified language ID.
	 * 
	 * @param 	languageID	The ID of the language to load.
	 */
	public static void loadLanguage(String languageID)
	{
		//Clear old data.
		translationsMap.clear();

		//Get the name and location of the appropriate language file.
		languageName = LANGUAGE_MAP.get(getLanguageIDFromOptions());
		MCA.getInstance().log("Loading " + languageName + "...");

		try
		{
			properties.load(StringTranslate.class.getResourceAsStream("/assets/mca/language/" + languageName + ".properties"));

			//Loop through each item in the properties instance.
			for (final Map.Entry<Object, Object> entrySet : properties.entrySet())
			{
				//OMIT will make the language loader skip that phrase.
				if (!entrySet.getValue().toString().equalsIgnoreCase("OMIT"))
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
						translationsMap.put(entrySet.getKey().toString(), entrySet.getValue().toString());
					}
				}
			}

			//Clear the properties instance.
			properties.clear();

			addLocalizedItemNames();
			addLocalizedAchievementNames(languageID);

			LanguageRegistry.reloadLanguageTable();

			MCA.getInstance().log("Loaded " + translationsMap.size() + " phrases in " + languageName + ".");
		}

		catch (IOException e)
		{
			MCA.getInstance().quitWithException("IOException while loading language.", e);
		}
	}

	private static void addLocalizedItemNames()
	{
		LanguageRegistry.addName(MCA.getInstance().itemEngagementRing, LanguageHelper.getString("item.ring.engagement"));
		LanguageRegistry.addName(MCA.getInstance().itemWeddingRing, LanguageHelper.getString("item.ring.wedding"));
		LanguageRegistry.addName(MCA.getInstance().itemArrangersRing, LanguageHelper.getString("item.ring.arranger"));
		LanguageRegistry.addName(MCA.getInstance().itemTombstone, LanguageHelper.getString("item.tombstone"));
		LanguageRegistry.addName(MCA.getInstance().itemWhistle, LanguageHelper.getString("item.whistle"));
		LanguageRegistry.addName(MCA.getInstance().itemBabyBoy, LanguageHelper.getString("item.baby.boy"));
		LanguageRegistry.addName(MCA.getInstance().itemBabyGirl, LanguageHelper.getString("item.baby.girl"));
		LanguageRegistry.addName(MCA.getInstance().itemEggMale, LanguageHelper.getString("item.egg.male"));
		LanguageRegistry.addName(MCA.getInstance().itemEggFemale, LanguageHelper.getString("item.egg.female"));
		LanguageRegistry.addName(MCA.getInstance().itemVillagerEditor, LanguageHelper.getString("item.editor"));
		LanguageRegistry.addName(MCA.getInstance().itemLostRelativeDocument, LanguageHelper.getString("item.lostrelativedocument"));
		LanguageRegistry.addName(MCA.getInstance().itemCrown, LanguageHelper.getString("item.crown"));
		LanguageRegistry.addName(MCA.getInstance().itemHeirCrown, LanguageHelper.getString("item.heircrown"));
		LanguageRegistry.addName(MCA.getInstance().itemKingsCoat, LanguageHelper.getString("item.kingscoat"));
		LanguageRegistry.addName(MCA.getInstance().itemKingsPants, LanguageHelper.getString("item.kingspants"));
		LanguageRegistry.addName(MCA.getInstance().itemKingsBoots, LanguageHelper.getString("item.kingsboots"));
	}

	private static void addLocalizedAchievementNames(String languageID)
	{
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
	 * @param 	entity		The MCA entity that is speaking.
	 * @param 	phraseId	The ID of the string to retrieve.
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(AbstractEntity entity, String phraseId)
	{
		return getString(null, entity, phraseId, true, null, null);
	}

	/**
	 * Retrieves the specified string from the string translations map.
	 * 
	 * @param 	entity				The MCA entity that is speaking.
	 * @param 	phraseId			The ID of the string to retrieve.
	 * @param	useCharacterType	Should the entity's character type be inserted before the ID of the string?
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(AbstractEntity entity, String phraseId, boolean useCharacterType)
	{
		return getString(null, entity, phraseId, useCharacterType, null, null);
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
	 * @param 	phraseId			The ID of the string to retrieve.
	 * @param	useCharacterType	Should the entity's character type be inserted before the ID of the string?
	 * @param	prefix				The string that should be added to the beginning of the localized string.
	 * @param	suffix				The string that should be added to the end of the localized string.
	 * 
	 * @return	Returns parsed localized string matching the ID provided.
	 */
	public static String getString(EntityPlayer player, AbstractEntity entity, String phraseId, boolean useCharacterType, String prefix, String suffix)
	{
		final List<String> matchingValues = new ArrayList();
		String outputString = "";
		phraseId = phraseId.toLowerCase();

		//Check for call to getString on a server. Invalid as the player will receive an untranslated string.
		if (MCA.getInstance().isDedicatedServer && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createSayLocalizedPacket(player, entity, phraseId, useCharacterType, prefix, suffix), (Player)player);
			return "";
		}

		if (useCharacterType)
		{
			phraseId = entity.getCharacterType(MCA.getInstance().getIdOfPlayer(player)) + "." + phraseId;
		}

		//Loop through each item in the string translations map.
		for (final Map.Entry<String, String> entrySet : translationsMap.entrySet())
		{
			//Check if the entry's key contains the ID.
			if (entrySet.getKey().contains(phraseId))
			{
				//Then check if it completely equals the ID.
				if (entrySet.getKey().equals(phraseId))
				{
					//In this case, clear the values list and add only the value that equals the ID.
					matchingValues.clear();
					matchingValues.add(entrySet.getValue());
					break;
				}

				else //Otherwise just add the matching ID's value to the matching values list.
				{
					matchingValues.add(entrySet.getValue());
				}
			}
		}

		if (matchingValues.isEmpty())
		{
			outputString = "(" + phraseId + " not found)";
		}

		else
		{
			prefix = prefix == null ? "" : prefix;
			suffix = suffix == null ? "" : suffix;
			outputString = prefix + parseString(player, entity, matchingValues.get(MCA.rand.nextInt(matchingValues.size())) + suffix);
		}

		return outputString;
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
		WorldPropertiesManager manager = null;

		if (player != null)
		{
			manager = MCA.getInstance().playerWorldManagerMap.get(player.username);
			playerId = MCA.getInstance().getIdOfPlayer(player);
		}

		try
		{
			if (text.contains("%Name%"))
			{
				text = text.replace("%Name%", entity.name);
			}

			if (text.contains("%RelationToPlayer%"))
			{
				text = text.replace("%RelationToPlayer%", entity.familyTree.getMyRelationTo(playerId).toString());
			}

			if (text.contains("%RelationOfPlayer%"))
			{
				text = text.replace("%RelationOfPlayer%", entity.familyTree.getRelationOf(playerId).toString());
			}

			if (text.contains("%MotherName%"))
			{
				if (entity instanceof EntityPlayerChild)
				{
					final List<Integer> parents = entity.familyTree.getIDsWithRelation(EnumRelation.Parent);

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
						final List<Integer> parents = entity.familyTree.getIDsWithRelation(EnumRelation.Parent);
						final AbstractEntity parent1 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(0)));
						final AbstractEntity parent2 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(1)));

						if (parent1.isMale == parent2.isMale)
						{
							text = text.replace("%MotherName%", parent1.name);
						}

						else if (parent1.isMale)
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
					List<Integer> parents = entity.familyTree.getIDsWithRelation(EnumRelation.Parent);

					if (parents.get(0) < 0 && parents.get(1) < 0)
					{
						text = text.replace("%FatherName%", MCA.getInstance().playerWorldManagerMap.get(player.username).worldProperties.playerSpouseName);
					}

					//One of the parents is not a player (since this is a player child, no further logic is required.)
					//Always use the villager as the last name.
					else
					{
						try
						{
							final AbstractEntity parent = (AbstractEntity)entity.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(1)));
							text = text.replace("%FatherName%", parent.getTitle(0, false));
						}

						catch (NullPointerException e)
						{
							final AbstractEntity parent = (AbstractEntity)entity.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(0)));
							text = text.replace("%FatherName%", parent.getTitle(0, false));
						}
					}
				}

				else
				{
					try
					{
						final List<Integer> parents = entity.familyTree.getIDsWithRelation(EnumRelation.Parent);
						final AbstractEntity parent1 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(0)));
						final AbstractEntity parent2 = (AbstractEntity)entity.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(1)));

						if (parent1.isMale == parent2.isMale)
						{
							text = text.replace("%FatherName%", parent2.name);
						}

						else if (parent1.isMale)
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
				final AbstractEntity spouse = entity.familyTree.getRelativeAsEntity(EnumRelation.Spouse);
				text = text.replace("%SpouseRelation%", spouse.familyTree.getMyRelationTo(playerId).toString(spouse, spouse.isMale, true));
			}

			if (text.contains("%PlayerSpouseName%"))
			{
				//Check world properties to see if the player is married to another player or an NPC.
				if (manager.worldProperties.playerSpouseID > 0)
				{
					//Player married to NPC, so the NPC is provided.
					text = text.replace("%PlayerSpouseName%", entity.familyTree.getRelativeAsEntity(EnumRelation.Spouse).name);
				}

				else
				{
					text = text.replace("%PlayerSpouseName%", manager.worldProperties.playerSpouseName);
				}
			}

			if (text.contains("%VillagerSpouseName%"))
			{
				AbstractEntity spouse = entity.familyTree.getRelativeAsEntity(EnumRelation.Spouse);

				if (spouse != null)
				{
					text = text.replace("%VillagerSpouseName%", spouse.name);
				}
			}

			if (text.contains("%SpouseFullName%"))
			{
				final AbstractEntity spouse = entity.familyTree.getRelativeAsEntity(EnumRelation.Spouse);

				if (spouse != null)
				{
					text = text.replace("%SpouseFullName%", entity.familyTree.getRelativeAsEntity(EnumRelation.Spouse).getTitle(playerId, true));
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
				final AbstractChild child = (AbstractChild)entity;

				String oreName = "";

				switch (child.miningChore.oreType)
				{
				case 0: oreName = "Coal"; break;
				case 1: oreName = "Iron"; break;
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
				final AbstractChild child = (AbstractChild)entity;
				text = text.replace("%OreDistance%", String.valueOf(child.miningChore.distanceToOre));
			}

			if (text.contains("%OreX"))
			{
				final AbstractChild child = (AbstractChild)entity;
				text = text.replace("%OreX%", String.valueOf(child.miningChore.nearestX));
			}
			
			if (text.contains("%OreY"))
			{
				final AbstractChild child = (AbstractChild)entity;
				text = text.replace("%OreY%", String.valueOf(child.miningChore.nearestY));
			}
			
			if (text.contains("%OreZ"))
			{
				final AbstractChild child = (AbstractChild)entity;
				text = text.replace("%OreZ%", String.valueOf(child.miningChore.nearestZ));
			}
			
			if (text.contains("%ChildTitle%"))
			{
				if (entity.isMale)
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
				if (entity.isMale)
				{
					text = text.replace("%RandomName%", Utility.getRandomName(!entity.isMale));
				}

				else
				{
					text = text.replace("%RandomName%", Utility.getRandomName(entity.isMale));
				}
			}

			if (text.contains("%CallPlayerParent%"))
			{
				if (!MCA.getInstance().isDedicatedServer)
				{
					if (manager.worldProperties.playerGender.equals("Male"))
					{
						text = text.replace("%CallPlayerParent%", getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.male"));
					}

					else if (manager.worldProperties.playerGender.equals("Female"))
					{
						text = text.replace("%CallPlayerParent%", getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.female"));
					}
				}

				else
				{
					final WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.username);

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
				if (!MCA.getInstance().isDedicatedServer)
				{
					text = text.replace("%PlayerName%", manager.worldProperties.playerName);
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.username);
					text = text.replace("%PlayerName%", serverPropertiesManager.worldProperties.playerName);
				}
			}

			if (text.contains("%TruePlayerName%"))
			{
				text = text.replace("%TruePlayerName%", player.username);
			}

			if (text.contains("%ParentOpposite%"))
			{
				if (!MCA.getInstance().isDedicatedServer)
				{
					if (manager.worldProperties.playerGender.equals("Male"))
					{
						text = text.replace("%ParentOpposite%", getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.male"));
					}

					else if (manager.worldProperties.playerGender.equals("Female"))
					{
						text = text.replace("%ParentOpposite%", getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.female"));
					}
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.username);

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
				if (!MCA.getInstance().isDedicatedServer)
				{
					text = text.replace("%BabyName%", manager.worldProperties.babyName);
				}

				else
				{
					WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.username);
					text = text.replace("%BabyName%", serverPropertiesManager.worldProperties.babyName);
				}
			}

			if (text.contains("%MonarchTitle%"))
			{
				if (manager.worldProperties.playerGender.equals("Male"))
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
				text = text.replace("%MinecraftVersionNumber%", UpdateHandler.validGameVersions);
			}

			if (text.contains("%ModVersionNumber%"))
			{
				text = text.replace("%ModVersionNumber%", UpdateHandler.mostRecentVersion);
			}

			if (text.contains("%URL%"))
			{
				text = text.replace("%URL%", Constants.COLOR_BLUE + Constants.FORMAT_ITALIC + "http://goo.gl/4Kwohv" + Constants.FORMAT_RESET + Constants.COLOR_YELLOW);
			}

			if (text.contains("%LivingParent%"))
			{
				final List<Integer> parents = entity.familyTree.getIDsWithRelation(EnumRelation.Parent);

				int parent1Id = -1;
				int parent2Id = -1;

				for (final Map.Entry<Integer, Integer> entry : MCA.getInstance().idsMap.entrySet())
				{
					final int keyInt = entry.getKey();
					final int valueInt = entry.getValue();

					if (keyInt == parents.get(0))
					{
						parent1Id = valueInt;
					}

					else if (keyInt == parents.get(1))
					{
						parent2Id = valueInt;
					}
				}

				final AbstractEntity parent1 = (AbstractEntity) player.worldObj.getEntityByID(parent1Id);
				final AbstractEntity parent2 = (AbstractEntity) player.worldObj.getEntityByID(parent2Id);
				final AbstractEntity nonNullParent = parent1 != null ? parent1 : parent2 != null ? parent2 : null;

				text = text.replace("%LivingParent%", nonNullParent.name);
			}
		}

		catch (NullPointerException e)
		{
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
		String languageID = "";

		try 
		{
			reader = new BufferedReader(new FileReader(MCA.getInstance().runningDirectory + "/options.txt"));

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
				languageID = line.substring(5);
			}
		} 

		catch (FileNotFoundException e) 
		{
			MCA.getInstance().log("Could not find options.txt file. Defaulting to English.");
			languageID = "en_US";
		} 

		catch (IOException e)
		{
			MCA.getInstance().quitWithException("Error reading from Minecraft options.txt file.", e);
			languageID = null;
		}

		catch (NullPointerException e)
		{
			MCA.getInstance().log("NullPointerException while trying to read options.txt. Defaulting to English.");
			languageID = "en_US";
		}

		return languageID;
	}

	public static Map<String, String> getTranslations()
	{
		return translationsMap;
	}

	static
	{
		LANGUAGE_MAP.put("af_ZA", "Afrikaans");
		LANGUAGE_MAP.put("ar_SA", "Arabic");
		LANGUAGE_MAP.put("bg_BG", "Bulgarian");
		LANGUAGE_MAP.put("ca_ES", "Catalan");
		LANGUAGE_MAP.put("cs_CZ", "Czech");
		LANGUAGE_MAP.put("cy_GB", "Welsh");
		LANGUAGE_MAP.put("da_DK", "Danish");
		LANGUAGE_MAP.put("de_DE", "German");
		LANGUAGE_MAP.put("el_GR", "Greek");
		LANGUAGE_MAP.put("en_AU", "English");
		LANGUAGE_MAP.put("en_CA", "English");
		LANGUAGE_MAP.put("en_GB", "English");
		LANGUAGE_MAP.put("en_PT", "Pirate");
		LANGUAGE_MAP.put("en_US", "English");
		LANGUAGE_MAP.put("eo_UY", "Esperanto");
		LANGUAGE_MAP.put("es_AR", "Argentina Spanish");
		LANGUAGE_MAP.put("es_ES", "Spanish");
		LANGUAGE_MAP.put("es_MX", "Mexico Spanish");
		LANGUAGE_MAP.put("es_UY", "Uruguay Spanish");
		LANGUAGE_MAP.put("es_VE", "Venezuela Spanish");
		LANGUAGE_MAP.put("et_EE", "Estonian");
		LANGUAGE_MAP.put("eu_ES", "Basque");
		LANGUAGE_MAP.put("fi_FI", "Finnish");
		LANGUAGE_MAP.put("fr_FR", "French");
		LANGUAGE_MAP.put("fr_CA", "Canadian French");
		LANGUAGE_MAP.put("ga_IE", "Irish");
		LANGUAGE_MAP.put("gl_ES", "Galician");
		LANGUAGE_MAP.put("he_IL", "Hebrew");
		LANGUAGE_MAP.put("hi_IN", "Hindi");
		LANGUAGE_MAP.put("hr_HR", "Croatian");
		LANGUAGE_MAP.put("hu_HU", "Hungarian");
		LANGUAGE_MAP.put("id_ID", "Bahasa Indonesia");
		LANGUAGE_MAP.put("is_IS", "Icelandic");
		LANGUAGE_MAP.put("it_IT", "Italian");
		LANGUAGE_MAP.put("ja_JP", "Japanese");
		LANGUAGE_MAP.put("ka_GE", "Georgian");
		LANGUAGE_MAP.put("ko_KR", "Korean");
		LANGUAGE_MAP.put("ko_KO", "Cornish");
		LANGUAGE_MAP.put("lt_LT", "Lithuanian");
		LANGUAGE_MAP.put("lv_LV", "Latvian");
		LANGUAGE_MAP.put("ms_MY", "Malay");
		LANGUAGE_MAP.put("mt_MT", "Maltese");
		LANGUAGE_MAP.put("nl_NL", "Dutch");
		LANGUAGE_MAP.put("nn_NO", "Nynorsk");
		LANGUAGE_MAP.put("nb_NO", "Norwegian");
		LANGUAGE_MAP.put("pl_PL", "Polish");
		LANGUAGE_MAP.put("pt_BR", "Brazilian Portuguese");
		LANGUAGE_MAP.put("pt_PT", "Portuguese");
		LANGUAGE_MAP.put("qya_AA", "Quenya");
		LANGUAGE_MAP.put("ru_RU", "Russian");
		LANGUAGE_MAP.put("sk_SK", "Slovak");
		LANGUAGE_MAP.put("sl_SI", "Slovenian");
		LANGUAGE_MAP.put("sr_SP", "Serbian");
		LANGUAGE_MAP.put("sv_SE", "Swedish");
		LANGUAGE_MAP.put("th_TH", "Thai");
		LANGUAGE_MAP.put("tlh_AA", "Klingon");
		LANGUAGE_MAP.put("tr_TR", "Turkish");
		LANGUAGE_MAP.put("uk_UA", "Ukrainian");
		LANGUAGE_MAP.put("vi_VN", "Vietnamese");
		LANGUAGE_MAP.put("zh_CN", "Chinese Simplified");
		LANGUAGE_MAP.put("zh_TW", "Chinese Traditional");
	}
}

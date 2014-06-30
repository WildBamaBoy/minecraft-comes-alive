/*******************************************************************************
 * LanguageParser.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.lang;

import java.util.List;

import mca.core.MCA;
import mca.core.io.WorldPropertiesList;
import mca.core.util.Utility;
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.lang.ILanguageParser;
import com.radixshock.radixcore.lang.LanguageLoader;

/**
 * MCA's language parser.
 */
public class LanguageParser implements ILanguageParser
{
	@Override
	public String parseString(String text, Object... arguments)
	{
		final LanguageLoader languageLoader = MCA.getInstance().getLanguageLoader();
		
		int playerId = 0;
		final EntityPlayer player = (EntityPlayer) arguments[0];
		final AbstractEntity entity = (AbstractEntity) arguments[1];

		WorldPropertiesManager manager = null;
		WorldPropertiesList properties = null;
		
		if (player != null)
		{
			manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
			properties = (WorldPropertiesList)manager.worldPropertiesInstance;
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
						text = text.replace("%MotherName%", player.getCommandSenderName());
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
						final AbstractEntity parent1 = MCA.getInstance().entitiesMap.get(parents.get(0));
						final AbstractEntity parent2 = MCA.getInstance().entitiesMap.get(parents.get(1));
						
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
						text = languageLoader.getString("gui.info.family.parents.deceased");
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
						text = text.replace("%FatherName%", properties.playerSpouseName);
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
						final AbstractEntity parent1 = MCA.getInstance().entitiesMap.get(parents.get(0));
						final AbstractEntity parent2 = MCA.getInstance().entitiesMap.get(parents.get(1));

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
						text = languageLoader.getString("gui.info.family.parents.deceased");
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
				if (MCA.getInstance().getWorldProperties(manager).playerSpouseID > 0)
				{
					//Player married to NPC, so the NPC is provided.
					text = text.replace("%PlayerSpouseName%", entity.familyTree.getRelativeAsEntity(EnumRelation.Spouse).name);
				}

				else
				{
					text = text.replace("%PlayerSpouseName%", MCA.getInstance().getWorldProperties(manager).playerSpouseName);
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

				String oreName = child.miningChore.oreEntry.getOreName();

				if (MCA.getInstance().getLanguageLoader().isValidString(oreName))
				{
					text = text.replace("%OreType%", languageLoader.getString(oreName).toLowerCase());					
				}
				
				else
				{
					text = text.replace("%OreType%", oreName.toLowerCase());
				}
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
					text = text.replace("%ChildTitle%", languageLoader.getString("family.son"));
				}

				else
				{
					text = text.replace("%ChildTitle%", languageLoader.getString("family.daughter"));
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
				if (MCA.getInstance().getWorldProperties(manager).playerGender.equals("Male"))
				{
					text = text.replace("%CallPlayerParent%", languageLoader.getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.male"));
				}

				else if (MCA.getInstance().getWorldProperties(manager).playerGender.equals("Female"))
				{
					text = text.replace("%CallPlayerParent%", languageLoader.getString("parser." + entity.getCharacterType(playerId) + ".callplayerparent.female"));
				}
			}

			if (text.contains("%PlayerName%"))
			{
				WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
				WorldPropertiesList serverProperties = (WorldPropertiesList)serverPropertiesManager.worldPropertiesInstance;
				text = text.replace("%PlayerName%", serverProperties.playerName);
			}

			if (text.contains("%TruePlayerName%"))
			{
				text = text.replace("%TruePlayerName%", player.getCommandSenderName());
			}

			if (text.contains("%ParentOpposite%"))
			{
				WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
				WorldPropertiesList serverProperties = (WorldPropertiesList)serverPropertiesManager.worldPropertiesInstance;
				
				if (serverProperties.playerGender.equals("Male"))
				{
					text = text.replace("%ParentOpposite%", languageLoader.getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.male"));
				}

				else if (serverProperties.playerGender.equals("Female"))
				{
					text = text.replace("%ParentOpposite%", languageLoader.getString("parser." + entity.getCharacterType(playerId) + ".parentopposite.female"));
				}
			}

			if (text.contains("%BabyName%"))
			{
				WorldPropertiesManager serverPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
				WorldPropertiesList serverProperties = (WorldPropertiesList)serverPropertiesManager.worldPropertiesInstance;
				text = text.replace("%BabyName%", serverProperties.babyName);
			}

			if (text.contains("%MonarchTitle%"))
			{
				if (MCA.getInstance().getWorldProperties(manager).playerGender.equals("Male"))
				{
					text = text.replace("%MonarchTitle%", languageLoader.getString("monarch.title.male.player"));
				}

				else
				{
					text = text.replace("%MonarchTitle%", languageLoader.getString("monarch.title.female.player"));
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

			if (text.contains("%LivingParent%"))
			{
				final List<Integer> parents = entity.familyTree.getIDsWithRelation(EnumRelation.Parent);
				final AbstractEntity parent1 = MCA.getInstance().entitiesMap.get(parents.get(0));
				final AbstractEntity parent2 = MCA.getInstance().entitiesMap.get(parents.get(1));
				AbstractEntity nonNullParent = parent1 != null ? parent1 : parent2 != null ? parent2 : null;

				if (nonNullParent == parent1 && parent1.isDead)
				{
					nonNullParent = parent2;
				}
				
				if (nonNullParent == parent2 && parent2.isDead)
				{
					nonNullParent = parent1;
				}
				
				if (!nonNullParent.isDead)
				{
					text = text.replace("%LivingParent%", nonNullParent.name);
				}
				
				else
				{
					text = languageLoader.getString("gui.info.family.parents.deceased");
				}
			}
		}

		catch (NullPointerException e)
		{
			text += " (Parsing error)";
		}

		return text;
	}
}

/*******************************************************************************
 * FamilyTree.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import com.radixshock.radixcore.file.WorldPropertiesManager;

/**
 * Handles information about each person's family.
 */
public class FamilyTree implements Serializable, Cloneable
{
	/** The actual owner of this family tree.*/
	public transient AbstractEntity owner;

	/** Map containing the IDs of entities related to the owner as the key, and their relation to the owner as the value.*/
	private Map<Integer, EnumRelation> relationMap;

	/**
	 * Constructor
	 * 
	 * @param	entity	The owner of the family tree.
	 */
	public FamilyTree(AbstractEntity entity)
	{
		owner = entity;
		relationMap = new HashMap<Integer, EnumRelation>();
	}

	/**
	 * Gets the opposing relation of the specified relation value.
	 * For instance, if someone is your MOTHER, then you are their SON or DAUGHTER.
	 * 
	 * @param	isMale		The gender of the entity who should have the opposing relation that is returned.	
	 * @param 	relation	The relation to find the opposing relation of.
	 * 
	 * @return	EnumRelation constant that is the opposing relation of the specified relation value.
	 */
	public static EnumRelation getOpposingRelation(boolean isMale, EnumRelation relation)
	{
		switch (relation)
		{
		case Aunt:
			return isMale ? EnumRelation.Nephew : EnumRelation.Niece;
		case Brother:
			return isMale ? EnumRelation.Brother : EnumRelation.Sister;
		case Cousin:
			return EnumRelation.Cousin;
		case Daughter:
			return isMale ? EnumRelation.Father : EnumRelation.Mother;
		case Father:
			return isMale ? EnumRelation.Son : EnumRelation.Daughter;
		case Granddaughter:
			return isMale ? EnumRelation.Grandfather : EnumRelation.Grandmother;
		case Grandson:
			return isMale ? EnumRelation.Grandfather : EnumRelation.Grandmother;
		case Greatgranddaughter:
			return isMale ? EnumRelation.Greatgrandfather : EnumRelation.Greatgrandmother;
		case Greatgrandson:
			return isMale ? EnumRelation.Greatgrandfather : EnumRelation.Greatgrandmother;
		case Husband:
			return isMale ? EnumRelation.Husband : EnumRelation.Wife;
		case Mother:
			return isMale ? EnumRelation.Son : EnumRelation.Daughter;
		case Nephew:
			return isMale ? EnumRelation.Uncle : EnumRelation.Aunt;
		case Niece:
			return isMale ? EnumRelation.Uncle : EnumRelation.Aunt;
		case Sister:
			return isMale ? EnumRelation.Brother : EnumRelation.Sister;
		case Son:
			return isMale ? EnumRelation.Father : EnumRelation.Mother;
		case Uncle:
			return isMale ? EnumRelation.Nephew : EnumRelation.Niece;
		case Wife:
			return isMale ? EnumRelation.Husband : EnumRelation.Wife;
		case Spouse:
			return isMale ? EnumRelation.Husband : EnumRelation.Wife;
		case Grandparent:
			return isMale ? EnumRelation.Grandson : EnumRelation.Granddaughter;
		case Greatgrandparent:
			return isMale ? EnumRelation.Greatgrandson : EnumRelation.Greatgranddaughter;
		case Parent:
			return isMale ? EnumRelation.Son : EnumRelation.Daughter;
		default:
			break;
		}

		return EnumRelation.None;
	}

	/**
	 * Adds the specified entity and relation value to the family tree.
	 * 
	 * @param 	player		The player being added to the family tree.
	 * @param 	relation	The relation to the owner of the family tree.
	 */
	public void addFamilyTreeEntry(EntityPlayer player, EnumRelation relation)
	{
		relationMap.put(MCA.getInstance().getIdOfPlayer(player), relation);
	}

	/**
	 * Adds the specified entity and relation value to the family tree.
	 * 
	 * @param 	entity		The entity being added to the family tree.
	 * @param 	relation	The entity's relation to the owner of the family tree.
	 */
	public void addFamilyTreeEntry(AbstractEntity entity, EnumRelation relation)
	{
		if (entity != null)
		{
			relationMap.put(entity.mcaID, relation);
		}
	}

	/**
	 * Adds the specified int and relation value to the family tree.
	 * 
	 * @param 	idToAdd			The ID to add to the family tree.
	 * @param 	relation	The relation of the entity with the specified ID to the owner of the family tree.
	 */
	public void addFamilyTreeEntry(int idToAdd, EnumRelation relation)
	{
		relationMap.put(idToAdd, relation);
	}

	/**
	 * Removes the provided player from the family tree.
	 * 
	 * @param 	player	The player to remove from the family tree.
	 */
	public void removeFamilyTreeEntry(EntityPlayer player)
	{
		relationMap.remove(MCA.getInstance().getIdOfPlayer(player));
	}

	/**
	 * Removes the provided entity from the family tree.
	 * 
	 * @param 	entity	The entity to remove from the family tree.
	 */
	public void removeFamilyTreeEntry(AbstractEntity entity)
	{
		relationMap.remove(entity.mcaID);
	}

	/**
	 * Removes the provided ID from the family tree.
	 * 
	 * @param 	mcaId	The ID to remove from the family tree.
	 */
	public void removeFamilyTreeEntry(int mcaId)
	{
		relationMap.remove(mcaId);
	}

	/**
	 * Removes the provided EnumRelation from the family tree.
	 * 
	 * @param 	relation	The EnumRelation to remove from the family tree.
	 */
	public void removeFamilyTreeEntry(EnumRelation relation)
	{
		int removalKey = 0;

		for(final Map.Entry<Integer, EnumRelation> entry : relationMap.entrySet())
		{
			if (entry.getValue().equals(relation))
			{
				removalKey = entry.getKey();
			}
		}

		relationMap.remove(removalKey);
	}

	/**
	 * Checks if an entity is related to the owner of this family tree.
	 * 
	 * @param	entity	The entity being checked for relation to the owner.
	 * 
	 * @return	boolean identifying whether or not the provided entity is related to the owner of the family tree.
	 */
	public boolean entityIsARelative(AbstractEntity entity)
	{
		return relationMap.containsKey(entity.mcaID);
	}

	/**
	 * Checks if an id is related to the owner of this family tree.
	 * 
	 * @param	mcaID	The entity id being checked for relation to the owner.
	 * 
	 * @return	boolean identifying whether or not the provided entity is related to the owner of the family tree.
	 */
	public boolean idIsARelative(int mcaID)
	{
		return relationMap.containsKey(mcaID);
	}

	/**
	 * Gets a person's relation to the owner from the map.
	 * 
	 * @param 	entity	The entity whose relationship is being retrieved.
	 * 
	 * @return	The relation of the entity provided to the owner of this family tree.
	 */
	public EnumRelation getRelationOf(AbstractEntity entity)
	{
		if (entityIsARelative(entity))
		{
			return relationMap.get(entity.mcaID);
		}

		else
		{
			return EnumRelation.None;
		}
	}

	/**
	 * Gets a person's relation to the owner from the map.
	 * 
	 * @param 	mcaID	The ID of the entity.
	 * 
	 * @return	The relation of the entity with the provided ID.
	 */
	public EnumRelation getRelationOf(int mcaID)
	{
		if (idIsARelative(mcaID))
		{
			final EnumRelation returnRelation = relationMap.get(mcaID);

			if (returnRelation.equals(EnumRelation.Greatgrandparent))
			{
				if (mcaID < 0)
				{
					final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(MCA.getInstance().getPlayerByID(owner.worldObj, mcaID).getCommandSenderName());

					if (MCA.getInstance().getWorldProperties(manager).playerGender.equals("Male"))
					{
						return EnumRelation.Greatgrandfather;
					}

					else
					{
						return EnumRelation.Greatgrandmother;
					}
				}
			}

			else if (returnRelation.equals(EnumRelation.Grandparent))
			{
				if (mcaID < 0)
				{
					final EntityPlayer player = MCA.getInstance().getPlayerByID(owner.worldObj, mcaID);

					if (player != null)
					{
						final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());

						if (manager != null)
						{
							if (MCA.getInstance().getWorldProperties(manager).playerGender.equals("Male"))
							{
								return EnumRelation.Grandfather;
							}

							else
							{
								return EnumRelation.Grandmother;
							}
						}
					}
				}
			}

			return relationMap.get(mcaID);
		}

		else
		{
			return EnumRelation.None;
		}
	}

	/**
	 * Gets a person's relation to the owner from the map.
	 * 
	 * @param 	entity	The entity whose relationship is being retrieved.
	 * 
	 * @return	The relation of the entity provided to the owner of this family tree.
	 */
	public EnumRelation getMyRelationTo(AbstractEntity entity)
	{
		if (idIsARelative(entity.mcaID))
		{
			return getOpposingRelation(owner.isMale, relationMap.get(entity.mcaID));
		}

		else
		{
			return EnumRelation.None;
		}
	}

	/**
	 * Gets a person's relation to the owner from the map.
	 * 
	 * @param 	mcaID	The ID of the entity.
	 * 
	 * @return	The relation of the entity with the provided ID.
	 */
	public EnumRelation getMyRelationTo(int mcaID)
	{
		if (idIsARelative(mcaID))
		{
			return getOpposingRelation(owner.isMale, relationMap.get(mcaID));
		}

		else
		{
			return EnumRelation.None;
		}
	}

	/**
	 * Gets the ID of the first entity contained in the family tree that has the specified relation.
	 * 
	 * @param	relation	The EnumRelation value that an entity should have.
	 * 
	 * @return	The entity's ID who has the specified relation to the owner of the family tree. 0 if one is not found.
	 */
	public int getFirstIDWithRelation(EnumRelation relation)
	{
		for (final Map.Entry<Integer, EnumRelation> entry : relationMap.entrySet())
		{
			if (entry.getValue() == relation)
			{
				return entry.getKey();
			}
		}

		return 0;
	}

	/**
	 * Get a list of all the entities contained in the family tree that have the specified relation.
	 * 
	 * @param	relation	The EnumRelation value that an entity should have.
	 * 
	 * @return	List containing the IDs of entities who have the specified relation to the owner of the family tree.
	 */
	public List<Integer> getIDsWithRelation(EnumRelation relation)
	{
		final List<Integer> returnList = new ArrayList<Integer>();

		for (final Map.Entry<Integer, EnumRelation> entry : relationMap.entrySet())
		{
			if (entry.getValue() == relation)
			{
				returnList.add(entry.getKey());
			}
		}

		return returnList;
	}

	/**
	 * Writes the entity's family tree to NBT.
	 * 
	 * @param	nbt	The NBT object that saves information about the entity.
	 */
	public void writeTreeToNBT(NBTTagCompound nbt)
	{
		int counter = 0;

		for(final Map.Entry<Integer, EnumRelation> KVP : relationMap.entrySet())
		{
			nbt.setInteger("familyTreeEntryID" + counter, KVP.getKey());
			nbt.setString("familyTreeEntryRelation" + counter, KVP.getValue().getValue());

			counter++;
		}
	}

	/**
	 * Reads the entity's family tree from NBT.
	 * 
	 * @param	nbt	The NBT object that reads information about the entity.
	 */
	public void readTreeFromNBT(NBTTagCompound nbt)
	{
		int counter = 0;

		while (true)
		{
			final int entryID = nbt.getInteger("familyTreeEntryID" + counter);
			final String entryRelation = nbt.getString("familyTreeEntryRelation" + counter);

			if (entryID == 0 && entryRelation.equals(""))
			{
				break;
			}

			else
			{
				relationMap.put(entryID, EnumRelation.getEnum(entryRelation));
				counter++;
			}
		}
	}

	/**
	 * Writes all information about this family tree to the console.
	 */
	public void dumpTreeContents()
	{
		MCA.getInstance().getLogger().log("Family tree of " + owner.name + ". MCA ID: " + owner.mcaID);

		for (final Map.Entry<Integer, EnumRelation> entry : relationMap.entrySet())
		{
			MCA.getInstance().getLogger().log(entry.getKey() + " : " + entry.getValue().getValue());
		}
	}

	/**
	 * Gets an instance of the entity whose relation to this entity matches the provided relation.
	 * 
	 * @param 	relation	The relation of the entity that should be returned.
	 * 
	 * @return	Entity whose relation to this entity matches the provided relation.
	 */
	public AbstractEntity getRelativeAsEntity(EnumRelation relation) 
	{
		for (final Map.Entry<Integer, EnumRelation> entrySet : relationMap.entrySet())
		{
			for (final AbstractEntity entity : MCA.getInstance().entitiesMap.values())
			{
				if (entity.mcaID == entrySet.getKey() && entity.familyTree.getRelationOf(owner) == relation)
				{
					return entity;
				}
			}
		}

		return null;
	}

	/**
	 * Gets a list of all the players related to this entity.
	 * 
	 * @return	A list of all the players contained in the relation map.
	 */
	public List<Integer> getListOfPlayerIDs()
	{
		final List<Integer> returnList = new ArrayList<Integer>();

		for (final Integer integer : relationMap.keySet())
		{
			//All player IDs are negative.
			if (integer < 0 && !returnList.contains(integer))
			{
				returnList.add(integer);
			}
		}

		return returnList;
	}

	/**
	 * Sets the relation map of the family tree to the provided map value.
	 * 
	 * @param 	map	The map containing relation information.
	 */
	public void setRelationMap(Map<Integer, EnumRelation> map)
	{
		this.relationMap = map;
	}

	/**
	 * Returns a clone of this family tree.
	 * 
	 * @return	Value copy of the family tree referenced by this instance of FamilyTree.
	 */
	@Override
	public FamilyTree clone()
	{
		final FamilyTree returnTree = new FamilyTree(owner);
		returnTree.setRelationMap(relationMap);
		return returnTree;
	}
}

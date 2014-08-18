/*******************************************************************************
 * EnumRelation.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.enums;

import mca.core.MCA;
import mca.entity.AbstractEntity;

/**
 * Constants having to do with relationships to other people.
 * Some are currently unused.
 */
public enum EnumRelation
{
	None("None"),
	Son("Son"),
	Daughter ("Daughter"),
	Grandson ("Grandson"),
	Granddaughter ("Granddaughter"),
	Greatgrandson ("Great Grandson"),
	Greatgranddaughter ("Great Granddaughter"),
	Husband ("Husband"),
	Wife ("Wife"),
	Uncle ("Uncle"),
	Aunt ("Aunt"),
	Niece ("Niece"),
	Nephew ("Nephew"),
	Cousin ("Cousin"),
	Brother ("Brother"),
	Sister ("Sister"),
	Father ("Father"),
	Mother("Mother"),
	Spouse("Spouse"),
	Parent("Parent"),
	Grandparent("Grandparent"),
	Greatgrandparent("Great Grandparent"),
	Grandfather("Grandfather"),
	Grandmother("Grandmother"),
	Greatgrandfather("Great Grandfather"),
	Greatgrandmother("Great Grandmother");
	
	/** The actual string value of the enum constant */
	private String value;

	/**
	 * Adds an enum constant to this group of enums with the specified string value.
	 * 
	 * @param 	value	The string value of the enum being added.
	 */
	private EnumRelation(String value) 
	{
		this.value = value;
	}

	/**
	 * Gets the value of an enum constant.
	 * 
	 * @return	The value of the current enum constant.
	 */
	public String getValue() 
	{
		return value;
	}
	
	/**
	 * Gets localized representation of the current enum constant's value.
	 * 
	 * @return	Localized string of the enum's value.
	 */
	@Override
	public String toString() 
	{
		return MCA.getInstance().getLanguageLoader().getString("family." + this.getValue().toLowerCase().replaceAll(" ", ""));
	}
	
	/**
	 * Gets localized representation of the current enum constant's value.
	 * 
	 * Used to get the correct relation in the case of marriage, where the two people
	 * in the marriage simply know each other as "Spouse" in their family tree 
	 * instead of "Husband" and "Wife".
	 * 
	 * @param 	entity		The entity whose relation is being retreived.
	 * @param 	isMale		Is the entity male?
	 * @param	isInformal	Should localization use the informal version of the relation if applicable?
	 * 
	 * @return	Localized representation of the current enum constant's value.
	 */
	public String toString(AbstractEntity entity, boolean isMale, boolean isInformal)
	{
		if (value.equals("Mother") || value.equals("Father"))
		{
			if (isInformal)
			{
				return MCA.getInstance().getLanguageLoader().getString("family." + this.getValue().toLowerCase().replaceAll(" ", "") + ".informal", null, entity, false);
			}
			
			else
			{
				return MCA.getInstance().getLanguageLoader().getString("family." + this.getValue().toLowerCase().replaceAll(" ", "") + ".formal", null, entity, false);
			}
		}
		
	    else if (!value.equals("Spouse") && !value.equals("Parent"))
		{
			return MCA.getInstance().getLanguageLoader().getString( "family." + this.getValue().toLowerCase().replaceAll(" ", ""), null, entity, false);
		}
		
		else if (value.equals("Parent"))
		{
			if (isMale)
			{
				value = "Father";
			}
			
			else
			{
				value = "Mother";
			}
			
			return MCA.getInstance().getLanguageLoader().getString("family." + this.getValue().toLowerCase().replaceAll(" ", ""), null, entity, false);
		}
		
		else
		{			
			if (isMale)
			{
				value = "Husband";
			}
			
			else
			{
				value = "Wife";
			}
			
			return MCA.getInstance().getLanguageLoader().getString("family." + this.getValue().toLowerCase().replaceAll(" ", ""), null, entity, false);
		}
	}

	/**
	 * Gets the enum constant from a provided value.
	 * 
	 * @param 	value	The value to compare against all enum constants.
	 * 
	 * @return	EnumRelation whose value equals the provided value.
	 */
	public static EnumRelation getEnum(String value) 
	{
		if (value != null)
		{
			for (final EnumRelation relation : EnumRelation.values())
			{
				if (relation.getValue().equals(value))
				{
					return relation;
				}
			}
		}
		
		return null;
	}
}

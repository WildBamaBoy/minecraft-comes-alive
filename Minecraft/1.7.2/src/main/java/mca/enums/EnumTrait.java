/*******************************************************************************
 * EnumTrait.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.enums;

import mca.core.MCA;

/**
 * Applied to villagers at random to give them extra points and chance modifiers for certain interactions.
 */
public enum EnumTrait
{
	None("trait.none", 0),
	Shy("trait.shy", 1),
	Fun("trait.fun", 2),
	Serious("trait.serious", 3),
	Friendly("trait.friendly", 4),
	Irritable("trait.irritable", 5),
	Emotional("trait.emotional", 6),
	Outgoing("trait.outgoing", 7),
	Greedy("trait.greedy", 8);
	
	private String value;
	private int traitId;
	
	/**
	 * Constructor
	 * 
	 * @param 	value		The trait's ID within the language files.
	 * @param 	traitId		The trait's numeric ID.
	 */
	private EnumTrait(String value, int traitId)
	{
		this.value = value;
		this.traitId = traitId;
	}
	
	/**
	 * Gets a random trait, excluding the "None" trait.
	 * 
	 * @return	A random EnumTrait.
	 */
	public static EnumTrait getRandomTrait()
	{
		int randomId = MCA.rand.nextInt(EnumTrait.values().length);
		
		for (final EnumTrait trait : EnumTrait.values())
		{
			if (trait.getId() == randomId)
			{
				return trait;
			}
		}
		
		return None;
	}
	
	/**
	 * Gets the value of this EnumTrait.
	 * 
	 * @return	Value field of this EnumTrait. Value is ID within language files.	
	 */
	public String getValue()
	{
		return value;
	}
	
	/**
	 * Gets localized name of this EnumTrait.
	 * 
	 * @return	Localized name of this EnumTrait.
	 */
	public String getLocalizedValue()
	{
		return MCA.getInstance().getLanguageLoader().getString(value);
	}
	
	/**
	 * Returns ID value of this enum.
	 * 
	 * @return	ID field of this EnumTrait.
	 */
	public int getId()
	{
		return traitId;
	}
	
	/**
	 * Gets the appropriate trait for a villager based on the ID provided.
	 * 
	 * @param 	traitId	The trait's ID.
	 * 
	 * @return	EnumTrait with matching ID provided.
	 */
	public static EnumTrait getTraitById(int traitId)
	{
		for (final EnumTrait trait : EnumTrait.values())
		{
			if (trait.getId() == traitId)
			{
				return trait;
			}
		}
		
		return None;
	}
	
	/**
	 * Gets the amount to be added or removed from an interaction based on the trait.
	 * 
	 * @param 	interactionType	"chat", "joke", or "gift" based on which interaction is being performed.
	 * 
	 * @return	Integer amount that is added or subtracted from hearts modification value of interaction.
	 */
	public int getHeartsModifier(String interactionType) 
	{
		if (interactionType.equals("chat"))
		{
			switch (this)
			{
			case Emotional:
				return 4;
			case Friendly:
				return 9;
			case Fun:
				return 4;
			case Irritable:
				return -3;
			case None:
				return 0;
			case Outgoing:
				return 11;
			case Serious:
				return 3;
			case Shy:
				return -3;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("joke"))
		{
			switch (this)
			{
			case Emotional:
				return 0;
			case Friendly:
				return 4;
			case Fun:
				return 7;
			case Irritable:
				return -5;
			case None:
				return 0;
			case Outgoing:
				return 4;
			case Serious:
				return -3;
			case Shy:
				return -3;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("gift"))
		{
			switch (this)
			{
			case Emotional:
				return 3;
			case Friendly:
				return 11;
			case Fun:
				return 7;
			case Irritable:
				return 0;
			case None:
				return 0;
			case Outgoing:
				return 3;
			case Serious:
				return 0;
			case Shy:
				return 0;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("greeting"))
		{
			switch (this)
			{
			case Emotional:
				return 0;
			case Friendly:
				return 6;
			case Fun:
				return 0;
			case Irritable:
				return -2;
			case None:
				return 0;
			case Outgoing:
				return 11;
			case Serious:
				return 6;
			case Shy:
				return -3;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("story"))
		{
			switch (this)
			{
			case Emotional:
				return 3;
			case Friendly:
				return 3;
			case Fun:
				return 2;
			case Irritable:
				return -3;
			case None:
				return 0;
			case Outgoing:
				return 5;
			case Serious:
				return 2;
			case Shy:
				return -3;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("kiss"))
		{
			switch (this)
			{
			case Emotional:
				return 5;
			case Friendly:
				return 5;
			case Fun:
				return 0;
			case Irritable:
				return 0;
			case None:
				return 0;
			case Outgoing:
				return 0;
			case Serious:
				return 0;
			case Shy:
				return 2;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("flirt"))
		{
			switch (this)
			{
			case Emotional:
				return 2;
			case Friendly:
				return 3;
			case Fun:
				return 6;
			case Irritable:
				return 0;
			case None:
				return 0;
			case Outgoing:
				return 1;
			case Serious:
				return 0;
			case Shy:
				return 0;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("play"))
		{
			switch (this)
			{
			case Emotional:
				return 0;
			case Friendly:
				return 3;
			case Fun:
				return 8;
			case Irritable:
				return 0;
			case None:
				return 0;
			case Outgoing:
				return 3;
			case Serious:
				return 0;
			case Shy:
				return -2;
			default:
				return 0;
			}
		}
		
		return 0;
	}

	/**
	 * Gets modifier applied to chance of interaction success based on trait.
	 * 
	 * @param 	interactionType	"chat", "joke" or "gift" based on the type of interaction.
	 * 
	 * @return	Integer modifier added to base interaction success chance.
	 */
	public int getChanceModifier(String interactionType) 
	{
		if (interactionType.equals("chat"))
		{
			switch (this)
			{
			case Emotional:
				return 10;
			case Friendly:
				return 20;
			case Fun:
				return 15;
			case Irritable:
				return -10;
			case None:
				return 0;
			case Outgoing:
				return 30;
			case Serious:
				return 0;
			case Shy:
				return -20;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("joke"))
		{
			switch (this)
			{
			case Emotional:
				return -10;
			case Friendly:
				return 10;
			case Fun:
				return 30;
			case Irritable:
				return -20;
			case None:
				return 0;
			case Outgoing:
				return 10;
			case Serious:
				return -10;
			case Shy:
				return -10;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("gift"))
		{
			switch (this)
			{
			case Emotional:
				return 0;
			case Friendly:
				return 0;
			case Fun:
				return 0;
			case Irritable:
				return 0;
			case None:
				return 0;
			case Outgoing:
				return 0;
			case Serious:
				return 0;
			case Shy:
				return 0;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("greeting"))
		{
			switch (this)
			{
			case Emotional:
				return 0;
			case Friendly:
				return 30;
			case Fun:
				return 0;
			case Irritable:
				return -30;
			case None:
				return 0;
			case Outgoing:
				return 15;
			case Serious:
				return 30;
			case Shy:
				return -30;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("story"))
		{
			switch (this)
			{
			case Emotional:
				return 15;
			case Friendly:
				return 30;
			case Fun:
				return 15;
			case Irritable:
				return -15;
			case None:
				return 0;
			case Outgoing:
				return 15;
			case Serious:
				return 15;
			case Shy:
				return -30;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("kiss"))
		{
			switch (this)
			{
			case Emotional:
				return 10;
			case Friendly:
				return 15;
			case Fun:
				return 0;
			case Irritable:
				return -5;
			case None:
				return 0;
			case Outgoing:
				return 0;
			case Serious:
				return 0;
			case Shy:
				return -5;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("flirt"))
		{
			switch (this)
			{
			case Emotional:
				return 10;
			case Friendly:
				return 5;
			case Fun:
				return 20;
			case Irritable:
				return -5;
			case None:
				return 0;
			case Outgoing:
				return 15;
			case Serious:
				return 0;
			case Shy:
				return -15;
			default:
				return 0;
			}
		}
		
		else if (interactionType.equals("play"))
		{
			switch (this)
			{
			case Emotional:
				return 0;
			case Friendly:
				return 5;
			case Fun:
				return 20;
			case Irritable:
				return -10;
			case None:
				return 0;
			case Outgoing:
				return 10;
			case Serious:
				return 0;
			case Shy:
				return -5;
			default:
				return 0;
			}
		}
		
		return 0;
	}
	
	/**
	 * Returns an integer that determines how long someone's positive mood will take to cool down.
	 * 
	 * @return	Floating point SUBTRACTED from positive moods.
	 */
	public float getPositiveCooldownModifier()
	{
		switch (this)
		{
		case Emotional:
			return 0.7F;
		case Friendly:
			return 0.2F;
		case Fun:
			return 0.3F;
		case Irritable:
			return 0.9F;
		case None:
			return 0.5F;
		case Outgoing:
			return 0.4F;
		case Serious:
			return 0.5F;
		case Shy:
			return 0.5F;
		default:
			return 0.5F;
		}
	}
	
	/**
	 * Returns an integer that determines how long someone's negative mood will take to cool down.
	 * 
	 * @return	Floating point SUBTRACTED from negative moods to make them lower.
	 */
	public float getNegativeCooldownModifier()
	{
		switch (this)
		{
		case Emotional:
			return 0.3F;
		case Friendly:
			return 0.9F;
		case Fun:
			return 0.7F;
		case Irritable:
			return 0.2F;
		case None:
			return 0.5F;
		case Outgoing:
			return 0.5F;
		case Serious:
			return 0.3F;
		case Shy:
			return 0.5F;
		default:
			return 0.5F;
		}
	}
}

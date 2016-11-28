/*******************************************************************************
 * EnumMood.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.enums;

import java.util.ArrayList;
import java.util.List;

import mca.core.MCA;

/**
 * Applied to villagers to modify chance of interaction success and bonus points applied.
 */
public enum EnumMood 
{
	Ecstatic("mood.happy.ecstatic"),
	Cheerful("mood.happy.cheerful"),
	Happy("mood.happy.happy"),
	Fine("mood.happy.fine"),
	Okay("mood.happy.okay"),
	Passive("mood.neutral.passive"),
	Blue("mood.sadness.blue"),
	Unhappy("mood.sadness.unhappy"),
	Sad("mood.sadness.sad"),
	Cheerless("mood.sadness.cheerless"),
	Depressed("mood.sadness.depressed"),
	Annoyed("mood.anger.annoyed"),
	Agitated("mood.anger.agitated"),
	Mad("mood.anger.mad"),
	Seething("mood.anger.seething"),
	Infuriated("mood.anger.infuriated");

	private String value;

	/**
	 * Constructor
	 * 
	 * @param 	value	The mood's ID within language files.
	 */
	private EnumMood(String value)
	{
		this.value = value;
	}

	/**
	 * Gets the appropriate mood for a villager based on the mood type.
	 * 
	 * @param moodType	The type of mood the villager should be in: happy, sadness, anger, or neutral.
	 * @param moodValue	The mood level that should be applied.
	 * 
	 * @return	EnumMood of the appropriate type based on provided type and value.
	 */
	public static EnumMood getMoodByPointValue(String moodType, float moodValue)
	{
		if (moodType.equals("happy"))
		{
			if (moodValue < 1.0F)
			{
				return Okay;
			}

			else if (moodValue >= 1.0F && moodValue < 2.0F)
			{
				return Okay;
			}

			else if (moodValue >= 2.0F && moodValue < 3.0F)
			{
				return Fine;
			}

			else if (moodValue >= 3.0F && moodValue < 4.0F)
			{
				return Happy;
			}

			else if (moodValue >= 4.0F && moodValue < 5.0F)
			{
				return Cheerful;
			}

			else
			{
				return Ecstatic;
			}
		}

		else if (moodType.equals("neutral"))
		{
			return Passive;
		}

		else if (moodType.equals("sadness"))
		{
			if (moodValue < 1.0F)
			{
				return Blue;
			}

			else if (moodValue >= 1.0F && moodValue < 2.0F)
			{
				return Blue;
			}

			else if (moodValue >= 2.0F && moodValue < 3.0F)
			{
				return Unhappy;
			}

			else if (moodValue >= 3.0F && moodValue < 4.0F)
			{
				return Sad;
			}

			else if (moodValue >= 4.0F && moodValue < 5.0F)
			{
				return Cheerless;
			}

			else
			{
				return Depressed;
			}
		}

		else if (moodType.equals("anger"))
		{
			if (moodValue < 1.0F)
			{
				return Annoyed;
			}

			else if (moodValue >= 1.0F && moodValue < 2.0F)
			{
				return Annoyed;
			}

			else if (moodValue >= 2.0F && moodValue < 3.0F)
			{
				return Agitated;
			}

			else if (moodValue >= 3.0F && moodValue < 4.0F)
			{
				return Mad;
			}

			else if (moodValue >= 4.0F && moodValue < 5.0F)
			{
				return Seething;
			}

			else
			{
				return Infuriated;
			}
		}

		else
		{
			return null;
		}
	}

	/**
	 * Gets all possible moods as a list.
	 * 
	 * @return	ArrayList containing each possible EnumMood.
	 */
	public static List<EnumMood> getMoodsAsCyclableList()
	{
		final List<EnumMood> moods = new ArrayList<EnumMood>();

		moods.add(Ecstatic);
		moods.add(Cheerful);
		moods.add(Happy);
		moods.add(Fine);
		moods.add(Okay);
		
		moods.add(Passive);
		
		moods.add(Depressed);
		moods.add(Cheerless);
		moods.add(Sad);
		moods.add(Unhappy);
		moods.add(Blue);
		
		moods.add(Infuriated);
		moods.add(Seething);
		moods.add(Mad);
		moods.add(Agitated);
		moods.add(Annoyed);
		
		return moods;
	}

	/**
	 * Returns the mood's ID within the language files.
	 * 
	 * @return	mood.[mood type].[mood name]
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Queries the language helper to get the translated name of this mood.
	 * 
	 * @return	Localized name of the mood.
	 */
	public String getLocalizedValue()
	{
		return MCA.getInstance().getLanguageLoader().getString(value);
	}

	/**
	 * Is the mood one of the anger moods?
	 * 
	 * @return	True or false.
	 */
	public boolean isAnger()
	{
		return getValue().contains("anger");
	}

	/**
	 * Is the mood one of the sadness moods?
	 * 
	 * @return	True or false.
	 */
	public boolean isSadness()
	{
		return getValue().contains("sadness");
	}

	/**
	 * Is the mood neutral?
	 * 
	 * @return	True or false.
	 */
	public boolean isNeutral()
	{
		return getValue().contains("neutral");
	}

	/**
	 * Is the mood one of the happy moods?
	 * 
	 * @return	True or false.
	 */
	public boolean isHappy()
	{
		return getValue().contains("happy");
	}

	/**
	 * Returns the base mood level of this mood. Used for hearts and chance bonuses.
	 * 
	 * @return	1 - 5 depending on mood level.
	 */
	public int getMoodLevel()
	{
		switch (this)
		{
		case Agitated:
			return 2;
		case Annoyed:
			return 1;
		case Blue:
			return 1;
		case Cheerful:
			return 4;
		case Cheerless:
			return 4;
		case Depressed:
			return 5;
		case Ecstatic:
			return 5;
		case Fine:
			return 2;
		case Happy:
			return 3;
		case Infuriated:
			return 5;
		case Mad:
			return 3;
		case Okay:
			return 1;
		case Passive:
			return 1;
		case Sad:
			return 3;
		case Seething:
			return 4;
		case Unhappy:
			return 2;
		default:
			return 0;
		}
	}

	/**
	 * Gets the amount to be applied to chance of interaction success. Gift is used to calculate chance of refusal.
	 * 
	 * @param 	interactionType	"chat", "joke", or "gift" depending on the interaction being performed.
	 * 
	 * @return	Hearts modifier based on mood and mood level. Amount is checked for validity by interaction before being applied.
	 */
	public int getChanceModifier(String interactionType) 
	{
		if (interactionType.equals("chat"))
		{
			if (this.isAnger())
			{
				return -(20 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 5 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return -(20 * getMoodLevel());
			}
		}

		else if (interactionType.equals("joke"))
		{
			if (this.isAnger())
			{
				return -(20 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 5 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return -(20 * getMoodLevel());
			}
		}

		//This is only for chance of gift refusal.
		else if (interactionType.equals("gift"))
		{
			if (this.isAnger())
			{
				return 20 * getMoodLevel();
			}

			else if (this.isHappy())
			{
				return 0;
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 0;
			}
		}

		else if (interactionType.equals("greeting"))
		{
			if (this.isAnger())
			{
				return -(20 * getMoodLevel());
			}
			
			else if (this.isHappy())
			{
				return 5 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 0;
			}
		}

		else if (interactionType.equals("story"))
		{
			if (this.isAnger())
			{
				return -(20 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 5 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 0;
			}
		}
		
		else if (interactionType.equals("kiss"))
		{
			if (this.isAnger())
			{
				return -(20 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 5 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 5 * getMoodLevel();
			}			
		}
		
		else if (interactionType.equals("flirt"))
		{
			if (this.isAnger())
			{
				return -(20 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 3 * getMoodLevel();
			}			
		}

		else if (interactionType.equals("play"))
		{
			if (this.isAnger())
			{
				return -(5 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 3 * getMoodLevel();
			}
		}
		
		return 0;
	}

	/**
	 * Gets the amount to add to hearts based on mood.
	 * 
	 * @param 	interactionType	"chat", "joke", or "gift" depending on the interaction being performed.
	 * 
	 * @return	Hearts modifier based on mood and mood level. Amount is checked for validity by interaction before being applied.
	 */
	public int getHeartsModifier(String interactionType) 
	{
		if (interactionType.equals("chat"))
		{
			if (this.isAnger())
			{
				return -(3 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return -(6 * getMoodLevel());
			}
		}

		else if (interactionType.equals("joke"))
		{
			if (this.isAnger())
			{
				return -(6 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return -(6 * getMoodLevel());
			}
		}

		else if (interactionType.equals("gift"))
		{
			if (this.isAnger())
			{
				return -(6 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 6 * getMoodLevel();
			}
		}

		else if (interactionType.equals("greeting"))
		{
			if (this.isAnger())
			{
				return 0;
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 0;
			}
		}
		
		else if (interactionType.equals("story"))
		{
			if (this.isAnger())
			{
				return -(3 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 3 * getMoodLevel();
			}
		}
		
		else if (interactionType.equals("kiss"))
		{
			if (this.isAnger())
			{
				return -(3 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 3 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 3 * getMoodLevel();
			}
		}
		
		else if (interactionType.equals("flirt"))
		{
			if (this.isAnger())
			{
				return -(2 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 2 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 2 * getMoodLevel();
			}
		}

		else if (interactionType.equals("play"))
		{
			if (this.isAnger())
			{
				return -(1 * getMoodLevel());
			}

			else if (this.isHappy())
			{
				return 2 * getMoodLevel();
			}

			else if (this.isNeutral())
			{
				return 0;
			}

			else if (this.isSadness())
			{
				return 2 * getMoodLevel();
			}
		}
		
		return 0;
	}
}

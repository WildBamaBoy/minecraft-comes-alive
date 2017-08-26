package mca.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mca.core.MCA;

public enum EnumPersonality 
{
	//Fallback on error.
	UNASSIGNED(0, EnumMoodGroup.UNASSIGNED),

	//Positive
	ATHLETIC(1, EnumMoodGroup.PLAYFUL),  	//Runs 15% faster
	CONFIDENT(2, EnumMoodGroup.SERIOUS),	//Deals more attack damage
	STRONG(3, EnumMoodGroup.SERIOUS),		//Deals double attack damage
	FRIENDLY(4, EnumMoodGroup.GENERAL),   	//Bonus 15% points to all interactions

	//Neutral
	CURIOUS(21, EnumMoodGroup.SERIOUS), 	//Finds more on chores 
	PEACEFUL(22, EnumMoodGroup.GENERAL),   	//Will not fight.
	FLIRTY(23, EnumMoodGroup.PLAYFUL),		//Bonus 25% points to all interactions
	WITTY(24, EnumMoodGroup.PLAYFUL),		//None.

	//Negative
	SENSITIVE(41, EnumMoodGroup.GENERAL),  	//Chance of having feelings hurt and drastically dropping hearts.
	GREEDY(42, EnumMoodGroup.SERIOUS),		//Finds less on chores
	STUBBORN(43, EnumMoodGroup.SERIOUS),  	//15% more difficult to interact with.
	ODD(44, EnumMoodGroup.PLAYFUL);			//None.

	private int id;
	private EnumMoodGroup moodGroup;

	private EnumPersonality(int id, EnumMoodGroup moodGroup)
	{
		this.id = id;
		this.moodGroup = moodGroup;
	}

	public int getId()
	{
		return this.id;
	}

	public EnumMoodGroup getMoodGroup()
	{
		return this.moodGroup;
	}

	public static EnumPersonality getById(int id)
	{
		for (EnumPersonality personality : EnumPersonality.values())
		{
			if (personality.id == id)
			{
				return personality;
			}
		}

		return UNASSIGNED;
	}

	public static EnumPersonality getAtRandom()
	{
		List<EnumPersonality> validList = new ArrayList<EnumPersonality>();

		for (EnumPersonality personality : EnumPersonality.values())
		{
			if (personality.id != 0)
			{
				validList.add(personality);
			}
		}

		return validList.get(new Random().nextInt(validList.size()));
	}

	public static List<Integer> getListOfIds()
	{
		List<Integer> returnList = new ArrayList<Integer>();
		
		for (EnumPersonality personality : EnumPersonality.values())
		{
			if (personality != UNASSIGNED)
			{
				returnList.add(personality.id);
			}
		}
		
		return returnList;
	}
	
	public String getFriendlyName()
	{
		String name = "personality." + this.name().toLowerCase();
		return MCA.getLocalizer().getString(name);
	}

	public int getSuccessModifierForInteraction(EnumInteraction interaction) 
	{
		switch (interaction)
		{
		case CHAT: 
			return moodGroup == EnumMoodGroup.GENERAL ? 15 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 5 : 0;
		case JOKE: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 15 : moodGroup == EnumMoodGroup.SERIOUS ? -5 : 0;
		case SHAKE_HAND: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 15 : 0;
		case TELL_STORY: 
			return moodGroup == EnumMoodGroup.GENERAL ? 10 : moodGroup == EnumMoodGroup.PLAYFUL ? 0 : moodGroup == EnumMoodGroup.SERIOUS ? 10 : 0;
		case FLIRT: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 10 : moodGroup == EnumMoodGroup.SERIOUS ? -5 : 0;
		case HUG: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 10 : moodGroup == EnumMoodGroup.SERIOUS ? -5 : 0;
		case KISS: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 10 : moodGroup == EnumMoodGroup.SERIOUS ? -5 : 0;
		default:
			break;
		}

		return 0;
	}

	public int getHeartsModifierForInteraction(EnumInteraction interaction) 
	{
		switch (interaction)
		{
		case CHAT: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 1 : moodGroup == EnumMoodGroup.SERIOUS ? 2 : 0;
		case JOKE: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 3 : moodGroup == EnumMoodGroup.SERIOUS ? -1 : 0;
		case SHAKE_HAND: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? -1 : moodGroup == EnumMoodGroup.SERIOUS ? 2 : 0;
		case TELL_STORY: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? -2 : moodGroup == EnumMoodGroup.SERIOUS ? 4 : 0;
		case FLIRT: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 1 : moodGroup == EnumMoodGroup.SERIOUS ? -1 : 0;
		case HUG: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 1 : moodGroup == EnumMoodGroup.SERIOUS ? -1 : 0;
		case KISS: 
			return moodGroup == EnumMoodGroup.GENERAL ? 0 : moodGroup == EnumMoodGroup.PLAYFUL ? 2 : moodGroup == EnumMoodGroup.SERIOUS ? -2 : 0;
		default:
			break;
		}

		return 0;
	}
}

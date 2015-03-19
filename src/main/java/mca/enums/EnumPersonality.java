package mca.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mca.core.MCA;

public enum EnumPersonality 
{
	//Fallback on error.
	NONE(0, EnumMoodGroup.ALL),

	//Positive
	ATHLETIC(1, EnumMoodGroup.PLAYFUL),  	//Runs 15% faster
	CONFIDENT(2, EnumMoodGroup.SERIOUS),	//Deals more attack damage TODO
	STRONG(3, EnumMoodGroup.SERIOUS),		//Deals double attack damage TODO
	FRIENDLY(4, EnumMoodGroup.GENERAL),   	//Bonus to all interactions TODO

	//Neutral
	CURIOUS(21, EnumMoodGroup.SERIOUS), 	//Finds more on chores 	TODO
	PEACEFUL(22, EnumMoodGroup.GENERAL),   	//Combat chore disabled TODO
	FLIRTY(23, EnumMoodGroup.PLAYFUL),		//TODO
	WITTY(24, EnumMoodGroup.PLAYFUL),		//TODO

	//Negative
	SENSITIVE(41, EnumMoodGroup.GENERAL),  	//Chance of having feelings hurt and drastically dropping hearts. TODO
	GREEDY(42, EnumMoodGroup.SERIOUS),		//Finds less on chores TODO
	STUBBORN(43, EnumMoodGroup.SERIOUS),  	//10% chance of not following or staying  TODO
	ODD(44, EnumMoodGroup.PLAYFUL);			//TODO

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

		return NONE;
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

	public String getFriendlyName()
	{
		String name = "personality." + this.name().toLowerCase();
		return MCA.getLanguageManager().getString(name);
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

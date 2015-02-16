package mca.enums;

public enum EnumMoodGroup 
{
	ALL,
	GENERAL,
	PLAYFUL,
	SERIOUS;

	public EnumMood getMood(int level)
	{
		if (level == 0)
		{
			return EnumMood.PASSIVE;
		}

		else
		{
			for (EnumMood mood : EnumMood.values())
			{
				if (mood.getMoodGroup() == this && mood.getLevel() == level)
				{
					return mood;
				}
			}
		}

		return EnumMood.PASSIVE;
	}
}

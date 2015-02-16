package mca.enums;

import radixcore.helpers.LogicHelper;

public enum EnumBabyState 
{
	NONE(0),
	MALE(1),
	FEMALE(2);
	
	private int id;
	
	EnumBabyState(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumBabyState getRandomGender()
	{
		return LogicHelper.getBooleanWithProbability(50) ? MALE : FEMALE;
	}

	public static EnumBabyState fromId(int id)
	{
		for (EnumBabyState state : EnumBabyState.values())
		{
			if (state.id == id)
			{
				return state;
			}
		}
		
		return null;
	}
	
	public boolean isMale()
	{
		switch (this)
		{
		case MALE: return true;
		default: return false;
		}
	}
}

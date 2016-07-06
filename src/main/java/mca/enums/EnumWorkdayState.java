package mca.enums;

import radixcore.util.RadixMath;

public enum EnumWorkdayState 
{
	MOVE_INDOORS(1),
	WANDER(2),
	WATCH_CLOSEST_ANYTHING(3),
	WATCH_CLOSEST_PLAYER(4),
	IDLE(5),
	WORK(6);
	
	private int id;
	
	EnumWorkdayState(int id)
	{
		this.id = id;
	}
	
	public static EnumWorkdayState getById(int id)
	{
		for (EnumWorkdayState state : EnumWorkdayState.values())
		{
			if (state.id == id)
			{
				return state;
			}
		}
		
		return null;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumWorkdayState getRandom()
	{
		int idToReturn = RadixMath.getNumberInRange(1, 5);
		return getById(idToReturn);
	}
}

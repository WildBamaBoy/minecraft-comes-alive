package mca.enums;

import radixcore.util.RadixMath;

public enum EnumWorkdayState 
{
	WANDER (1), /* Wander through village */
	INDOORS (2), /* Move into a nearby building */
	WORK (3), /* Perform job */
	VISIT (4), /* Visit a far off building */
	CHAT (5); /* Stop and chat with nearby villager. */
	
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

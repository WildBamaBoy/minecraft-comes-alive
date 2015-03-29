package mca.enums;


public enum EnumSleepingState 
{
	AWAKE(0),
	INTERRUPTED(1),
	WALKING_HOME(2),
	SLEEPING(3),
	NO_HOME(4);
	
	private int id;
	
	EnumSleepingState(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumSleepingState fromId(int id)
	{
		for (EnumSleepingState state : EnumSleepingState.values())
		{
			if (state.id == id)
			{
				return state;
			}
		}
		
		return null;
	}
}

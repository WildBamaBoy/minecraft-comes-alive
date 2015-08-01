package mca.enums;


public enum EnumSleepingState 
{
	AWAKE(0),
	INTERRUPTED(1),
	SLEEPING(2),
	NO_HOME(3);
	
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

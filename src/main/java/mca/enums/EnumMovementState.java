package mca.enums;


public enum EnumMovementState 
{
	MOVE(0),
	STAY(1),
	FOLLOW(2);
	
	private int id;
	
	EnumMovementState(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumMovementState fromId(int id)
	{
		for (EnumMovementState state : EnumMovementState.values())
		{
			if (state.id == id)
			{
				return state;
			}
		}
		
		return null;
	}
}

package mca.enums;

public enum EnumMarriageState 
{
	UNASSIGNED(-1),
	NOT_MARRIED(0),
	ENGAGED(1),
	MARRIED_TO_PLAYER(2),
	MARRIED_TO_VILLAGER(3);
	
	int id = 0;
	
	EnumMarriageState(int id)
	{
		this.id = id;
	}
	
	public static EnumMarriageState byId(int id)
	{
		for (EnumMarriageState state : values())
		{
			if (state.id == id)
			{
				return state;
			}
		}
		
		return UNASSIGNED;
	}
	
	public int getId()
	{
		return id;
	}
}

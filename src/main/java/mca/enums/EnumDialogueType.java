package mca.enums;

public enum EnumDialogueType 
{
	NONE (-1),
	PARENT (0),
	RELATIVE (1),
	ADULT (2),
	CHILD (3),
	SPOUSE (4),
	PLAYERCHILD (5);
	
	private int id;
	
	EnumDialogueType(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumDialogueType getById(int id)
	{
		for (EnumDialogueType personality : EnumDialogueType.values())
		{
			if (personality.id == id)
			{
				return personality;
			}
		}
		
		return NONE; 
	}

	@Override
	public String toString() 
	{
		return this.name().toLowerCase();
	}
}

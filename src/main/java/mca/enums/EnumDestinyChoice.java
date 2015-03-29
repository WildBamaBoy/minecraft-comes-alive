package mca.enums;


public enum EnumDestinyChoice 
{
	FAMILY(1),
	ALONE(2),
	VILLAGE(3),
	NONE(4);
	
	private int id;
	
	EnumDestinyChoice(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}

	public static EnumDestinyChoice fromId(int id)
	{
		for (EnumDestinyChoice choice : EnumDestinyChoice.values())
		{
			if (choice.id == id)
			{
				return choice;
			}
		}
		
		return null;
	}
}

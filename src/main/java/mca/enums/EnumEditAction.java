package mca.enums;

public enum EnumEditAction 
{
	RANDOM_NAME(0),
	SWAP_GENDER(1),
	TEXTURE_UP(2),
	TEXTURE_DOWN(3),
	PROFESSION_UP(4),
	PROFESSION_DOWN(5),
	TRAIT_UP(6),
	TRAIT_DOWN(7),
	HEIGHT_UP(8),
	HEIGHT_DOWN(9),
	GIRTH_UP(10),
	GIRTH_DOWN(11),
	TOGGLE_INFECTED(12),
	SET_NAME(13);
	
	int id;
	
	EnumEditAction(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumEditAction byId(int id)
	{
		for (EnumEditAction action : values()) 
		{
			if (action.id == id)
			{
				return action;
			}
		}
		
		return null;
	}
}

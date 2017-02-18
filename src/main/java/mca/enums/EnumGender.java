package mca.enums;

public enum EnumGender 
{
	UNASSIGNED(0),
	MALE(1),
	FEMALE(2);
	
	int id;
	
	EnumGender(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumGender byId(int id)
	{
		for (EnumGender gender : values())
		{
			if (gender.id == id)
			{
				return gender;
			}
		}
		
		return UNASSIGNED;
	}
}

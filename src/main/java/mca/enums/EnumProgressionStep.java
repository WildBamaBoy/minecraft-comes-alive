package mca.enums;

public enum EnumProgressionStep 
{
	UNKNOWN(0),
	SEARCH_FOR_PARTNER(1),
	TRY_FOR_BABY(2),
	HAD_BABY(3),
	FINISHED(4);
	
	private int id;
	
	EnumProgressionStep(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public static EnumProgressionStep getFromId(int id)
	{
		for (EnumProgressionStep status : EnumProgressionStep.values())
		{
			if (status.getId() == id)
			{
				return status;
			}
		}
		
		return UNKNOWN;
	}
}

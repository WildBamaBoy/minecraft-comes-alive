package mca.enums;

public enum EnumMemorialType 
{
	BROKEN_RING(0, "BrokenRing"),
	DOLL(1, "ChildsDoll"),
	TRAIN(2, "ToyTrain");
	
	int id;
	String typeName;
	
	EnumMemorialType(int id, String typeName)
	{
		this.id = id;
		this.typeName = typeName;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getTypeName()
	{
		return typeName;
	}
	
	public static EnumMemorialType fromId(int id)
	{
		for (EnumMemorialType type : values())
		{
			if (type.id == id)
			{
				return type;
			}
		}
		
		return null;
	}
}

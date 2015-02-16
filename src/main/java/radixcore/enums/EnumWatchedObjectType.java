package radixcore.enums;

public enum EnumWatchedObjectType 
{
	Byte(0),
	Short(1),
	Integer(2),
	Float(3),
	String(4);
	
	private int id;
	
	EnumWatchedObjectType(int id)
	{
		this.id = id;
	}
	
	public static EnumWatchedObjectType getById(int id)
	{
		for (EnumWatchedObjectType type : EnumWatchedObjectType.values())
		{
			if (type.id == id)
			{
				return type;
			}
		}
		
		return null;
	}
}
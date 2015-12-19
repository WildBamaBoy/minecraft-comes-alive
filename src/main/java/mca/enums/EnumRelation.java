package mca.enums;

public enum EnumRelation 
{
	NONE(0),
	HUSBAND(1),
	WIFE(2),
	SON(3),
	DAUGHTER(4);
	
	private int id;
	
	private EnumRelation(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getPhraseId()
	{
		return "relation." + name().toLowerCase();
	}
	
	public static EnumRelation getById(int id)
	{
		for (EnumRelation relation : values())
		{
			if (relation.id == id)
			{
				return relation;
			}
		}
		
		return NONE;
	}
}

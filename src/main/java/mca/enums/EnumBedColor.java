package mca.enums;

public enum EnumBedColor 
{
	BLUE,
	GREEN,
	PINK,
	PURPLE,
	RED;
	
	@Override
	public String toString()
	{
		return name().toLowerCase().substring(0, 1).toUpperCase() + name().substring(1);
	}
}

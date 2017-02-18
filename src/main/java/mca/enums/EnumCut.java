package mca.enums;

public enum EnumCut 
{
	NONE,
	HEART,
	OVAL,
	SQUARE,
	TRIANGLE,
	TINY,
	STAR;
	
	@Override
	public String toString()
	{
		return this == NONE ? "" : name().toLowerCase().substring(0, 1).toUpperCase() + name().substring(1);
	}
}

package mca.enums;

import radixcore.helpers.StringHelper;

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
		return this == NONE ? "" : StringHelper.upperFirstLetter(name().toLowerCase());
	}
}

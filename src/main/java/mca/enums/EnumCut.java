package mca.enums;

import radixcore.util.RadixString;

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
		return this == NONE ? "" : RadixString.upperFirstLetter(name().toLowerCase());
	}
}

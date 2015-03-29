package mca.enums;

import radixcore.util.RadixString;

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
		return RadixString.upperFirstLetter(this.name().toLowerCase());
	}
}

package mca.enums;

import radixcore.helpers.StringHelper;

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
		return StringHelper.upperFirstLetter(this.name().toLowerCase());
	}
}

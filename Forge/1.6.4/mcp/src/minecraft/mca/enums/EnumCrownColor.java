package mca.enums;

public enum EnumCrownColor 
{
	Red("Red"),
	Green("Green"),
	Blue("Blue"),
	Pink("Pink"),
	Purple("Purple");
	
	private String colorName;
	
	private EnumCrownColor(String name)
	{
		this.colorName = name;
	}
	
	public String getColorName()
	{
		return colorName;
	}
}

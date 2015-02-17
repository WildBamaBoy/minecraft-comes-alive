package radixcore.constant;

public enum Color16 
{
	BLACK(0, "black", 0x1E1B1B),
	RED(1, "red", 0xB3312C), 
	GREEN(2, "green", 0x3B511A), 
	BROWN(3, "brown", 0x51301A), 
	BLUE(4, "blue", 0x253192), 
	PURPLE(5, "purple", 0x7B2FBE), 
	CYAN(6, "cyan", 0x287697), 
	SILVER(7, "silver", 0xABABAB), 
	GRAY(8, "gray", 0x434343), 
	PINK(9, "pink", 0xD88198), 
	LIME(10, "lime",  0x41CD34), 
	YELLOW(11, "yellow",  0xDECF2A), 
	LIGHT_BLUE(12, "light_blue", 0x6689D3),
	MAGENTA(13, "magenta", 0xC354CD),
	ORANGE(14, "orange", 0xEB8844),
	WHITE(15, "white", 0xF0F0F0);
	
	private int id;
	private String name;
	private int rgbValue;
	
	Color16(int id, String name, int rgbValue)
	{
		this.id = id;
		this.name = name;
		this.rgbValue = rgbValue;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getRGBValue()
	{
		return rgbValue;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static Color16 fromId(int id)
	{
		for (Color16 color : Color16.values())
		{
			if (color.getId() == id)
			{
				return color;
			}
		}
		
		return null;
	}
}

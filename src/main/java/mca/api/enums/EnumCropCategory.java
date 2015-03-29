package mca.api.enums;


/**
 * An enum that helps to map a compatible farm schematic to a particular type of crop.
 */
public enum EnumCropCategory 
{
	/**	
	 * A crop in this category will be treated like wheat. Small amounts of water will be required nearby, 
	 * and the plant will gradually grow on the same block by changing its metadata over time.
	 */
	WHEAT(4),
	
	/**
	 * A crop in this category grows into a stem, and produces fruit over time that occupy nearby blocks.
	 */
	MELON(1),
	
	/**
	 * A crop in this category is required to be beside water, and will grow upwards.
	 */
	SUGARCANE(13);
	
	private int referenceMeta;
	
	EnumCropCategory(int woolMeta)
	{
		this.referenceMeta = woolMeta;
	}
	
	public static EnumCropCategory getByWoolMeta(int meta)
	{
		for (EnumCropCategory type : EnumCropCategory.values())
		{
			if (type.referenceMeta == meta)
			{
				return type;
			}
		}
		
		return null;
	}
	
	public int getReferenceMeta()
	{
		return referenceMeta;
	}
}

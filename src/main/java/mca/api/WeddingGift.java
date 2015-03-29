package mca.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public final class WeddingGift 
{
	private final Item item;
	private final int minimum;
	private final int maximum;
	
	public WeddingGift(Item item, int minimum, int maximum)
	{
		this.item = item;
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	public WeddingGift(Block block, int minimum, int maximum)
	{
		this(Item.getItemFromBlock(block), minimum, maximum);
	}
	
	public Item getItem()
	{
		return item;
	}
	
	public int getMinimum()
	{
		return minimum;
	}
	
	public int getMaximum()
	{
		return maximum;
	}
}

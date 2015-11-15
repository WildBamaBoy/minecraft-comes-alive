package mca.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MiningEntry 
{
	private Block block;
	private ItemStack minedItemStack;
	private float percentileWeight;
	
	public MiningEntry(Block block, ItemStack minedItemStack, float percentileWeight)
	{
		this.block = block;
		this.minedItemStack = minedItemStack;
		this.percentileWeight = percentileWeight;
	}
	
	public MiningEntry(Block block, Item item, float percentileWeight)
	{
		this(block, new ItemStack(item), percentileWeight);
	}
	
	public MiningEntry(Block block, float percentileWeight)
	{
		this(block, (ItemStack) null, percentileWeight);
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public ItemStack getMinedItemStack()
	{
		if (minedItemStack != null)
		{
			return minedItemStack;
		}
		
		else
		{
			return new ItemStack(Item.getItemFromBlock(block));
		}
	}
	
	public float getWeight()
	{
		return percentileWeight;
	}
}

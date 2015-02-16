package radixcore.data;

import net.minecraft.block.Block;

public class BlockObj
{
	private Block block;
	private int meta;
	
	public BlockObj(Block block, int meta)
	{
		this.block = block;
		this.meta = meta;
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public int getMeta()
	{
		return meta;
	}
}

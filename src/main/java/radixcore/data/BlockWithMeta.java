package radixcore.data;

import net.minecraft.block.Block;

public class BlockWithMeta
{
	private Block block;
	private int meta;
	
	public BlockWithMeta(Block block, int meta)
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

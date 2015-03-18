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
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof BlockObj)
		{
			final BlockObj block = (BlockObj)obj;
			
			if (block.block == this.block && block.meta == this.meta)
			{
				return true;
			}
		}
		
		return false;
	}
}

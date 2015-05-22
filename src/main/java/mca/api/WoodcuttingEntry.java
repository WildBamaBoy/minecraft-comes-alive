package mca.api;

import net.minecraft.block.Block;

/**
 * Defines a tree type that can be cut by villagers.
 */
public final class WoodcuttingEntry 
{
	private final Block logBlock;
	private final Block saplingBlock;
	private final int logMeta;
	private final int saplingMeta;
	
	public WoodcuttingEntry(Block logBlock, int logMeta)
	{
		this.logBlock = logBlock;
		this.logMeta = logMeta;
		this.saplingBlock = null;
		this.saplingMeta = -1;
	}
	
	public WoodcuttingEntry(Block logBlock, int logMeta, Block saplingBlock, int saplingMeta)
	{
		this.logBlock = logBlock;
		this.logMeta = logMeta;
		this.saplingBlock = saplingBlock;
		this.saplingMeta = saplingMeta;
	}
	
	public Block getLogBlock()
	{
		return logBlock;
	}
	
	public Block getSaplingBlock()
	{
		return saplingBlock;
	}
	
	public int getLogMeta()
	{
		return logMeta;
	}
	
	public int getSaplingMeta()
	{
		return saplingMeta;
	}
	
	public boolean hasSapling()
	{
		return saplingBlock != null;
	}
}

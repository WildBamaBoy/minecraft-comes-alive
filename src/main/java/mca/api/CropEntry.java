package mca.api;

import javax.annotation.Nullable;

import mca.api.enums.EnumCropCategory;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class CropEntry 
{
	/** The crop's category defines how it will behave. See {@link mca.api.EnumCropCategory} for specifics. */
	private final EnumCropCategory category;
	
	/** The block that will be the planted crop. Not necessarily the harvested crop. */
	private final Block blockCrop;
	
	/** The item that will serve as the crop's seed. */
	private final Item itemSeed;
	
	/** The block that will be used to identify the crop as harvestable. */
	private final Block harvestBlock;
	
	/** The metadata that the <code>harvestBlock</code> must have in order for the block to be marked as harvestable. */
	private final int harvestBlockMeta;
	
	/** The item returned to the harvester (a villager) once a crop is harvested. If null, <code>harvestBlock:harvestBlockMeta</code> will be added to the harvester's inventory. */
	private final Item altHarvestItem;
	
	/** The minimum number of items yielded from one harvest of this crop. */
	private final int minYield;

	/** The maximum number of items yielded from one harvest of this crop. */
	private final int maxYield;
	
	/**
	 * Constructor for a new crop entry.
	 * 
	 * @param category			The category that the crop best fits in to.
	 * @param blockCrop			The crop's planted block.
	 * @param itemSeed			The crop's seed.
	 * @param harvestBlock		The crop's harvestable block.
	 * @param harvestBlockMeta	The harvestable block's metadata at the time of harvest.
	 * @param harvestItem		The item returned from harvesting. If null, <code>harvestBlock:harvestBlockMeta</code> will be placed in the harvester's inventory.
	 */
	public CropEntry(EnumCropCategory category, Block blockCrop, Item itemSeed, Block harvestBlock, int harvestBlockMeta, @Nullable Item altHarvestItem, int minYield, int maxYield)
	{
		this.category = category;
		this.blockCrop = blockCrop;
		this.itemSeed = itemSeed;
		this.harvestBlock = harvestBlock;
		this.harvestBlockMeta = harvestBlockMeta;
		this.altHarvestItem = altHarvestItem;
		this.minYield = minYield;
		this.maxYield = maxYield;
	}
	
	public ItemStack[] getStacksOnHarvest()
	{
		ItemStack[] returnArray = new ItemStack[2];
		int seedAmount = ChoreRegistry.rand.nextInt(2) + 1;
		int cropAmount = ChoreRegistry.rand.nextInt(maxYield) + minYield;
		
		if (altHarvestItem != null)
		{
			returnArray[0] = new ItemStack(altHarvestItem, cropAmount, 0);
			
			//Crops whose harvested item is the same as the seed will not get a seed return.
			if (itemSeed != altHarvestItem)
			{
				returnArray[1] = new ItemStack(itemSeed, seedAmount, 0);
			}
		}
		
		else
		{
			returnArray[0] = new ItemStack(harvestBlock, cropAmount, harvestBlockMeta);
			returnArray[1] = new ItemStack(itemSeed, seedAmount, 0);
		}
		
		return returnArray;
	}
	
	public String getCropName()
	{
		return I18n.format(itemSeed.getUnlocalizedName() + ".name");
	}
	
	public EnumCropCategory getCategory()
	{
		return category;
	}
	
	public Item getSeedItem()
	{
		return itemSeed;
	}
	
	public Block getCropBlock()
	{
		return blockCrop;
	}
	
	public Block getHarvestBlock()
	{
		return harvestBlock;
	}
	
	public int getHarvestBlockMeta()
	{
		return harvestBlockMeta;
	}
}

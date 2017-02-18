package mca.items;

import mca.core.MCA;
import mca.entity.EntityGrimReaper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.modules.RadixBlocks;

public class ItemSpawnGrimReaper extends Item
{
	public ItemSpawnGrimReaper()
	{
		final String itemName = "EggGrimReaper";

		this.setCreativeTab(MCA.getCreativeTabMain());
		this.setMaxStackSize(1);
		this.setUnlocalizedName(itemName);

		GameRegistry.registerItem(this, itemName);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		int posX = pos.getX();
		int posY = pos.getY() + 1;
		int posZ = pos.getZ();
		
		if (!worldIn.isRemote)
		{
			final Block block = RadixBlocks.getBlock(worldIn, posX, posY, posZ);
			double verticalOffset = 0.0D;

			EntityGrimReaper reaper = new EntityGrimReaper(worldIn);
			reaper.setPosition(posX + 0.5D, posY + verticalOffset, posZ + 0.5D);
			reaper.worldObj.spawnEntityInWorld(reaper);
			
			if (!playerIn.capabilities.isCreativeMode)
			{
				playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
			}
			
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
	}
}

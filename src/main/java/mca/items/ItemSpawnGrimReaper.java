package mca.items;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.MCA;
import mca.entity.EntityGrimReaper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import radixcore.util.BlockHelper;

public class ItemSpawnGrimReaper extends Item
{
	public ItemSpawnGrimReaper()
	{
		final String itemName = "EggGrimReaper";

		this.setCreativeTab(MCA.getCreativeTabMain());
		this.setMaxStackSize(1);
		this.setUnlocalizedName(itemName);
		this.setTextureName("mca:" + itemName);

		GameRegistry.registerItem(this, itemName);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (!world.isRemote)
		{
			final Block block = BlockHelper.getBlock(world, posX, posY, posZ);
			double verticalOffset = 0.0D;

			posX += Facing.offsetsXForSide[meta];
			posY += Facing.offsetsYForSide[meta];
			posZ += Facing.offsetsZForSide[meta];

			if (meta == 1 && block == Blocks.fence || block == Blocks.nether_brick_fence)
			{
				verticalOffset = 0.5D;
			}

			EntityGrimReaper reaper = new EntityGrimReaper(world);
			reaper.setPosition(posX + 0.5D, posY + verticalOffset, posZ + 0.5D);
			reaper.worldObj.spawnEntityInWorld(reaper);
			
			if (!player.capabilities.isCreativeMode)
			{
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}
			
			return true;
		}
		
		return false;
	}
}

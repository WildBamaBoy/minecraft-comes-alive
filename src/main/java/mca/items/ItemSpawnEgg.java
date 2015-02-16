package mca.items;

import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemSpawnEgg extends Item
{
	private boolean isMale;

	public ItemSpawnEgg(boolean isMale)
	{
		final String itemName = isMale ? "EggMale" : "EggFemale";

		this.isMale = isMale;
		this.setCreativeTab(MCA.getCreativeTab());
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
			final Block block = world.getBlock(posX, posY, posZ);
			double verticalOffset = 0.0D;

			posX += Facing.offsetsXForSide[meta];
			posY += Facing.offsetsYForSide[meta];
			posZ += Facing.offsetsZForSide[meta];

			if (meta == 1 && block == Blocks.fence || block == Blocks.nether_brick_fence)
			{
				verticalOffset = 0.5D;
			}

			spawnHuman(world, posX + 0.5D, posY + verticalOffset, posZ + 0.5D);
			
			if (!player.capabilities.isCreativeMode)
			{
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}
			
			return true;
		}
		
		return false;
	}

	public void spawnHuman(World world, double posX, double posY, double posZ)
	{
		EntityHuman entityHuman = new EntityHuman(world, isMale);
		entityHuman.setPosition(posX, posY, posZ);
		world.spawnEntityInWorld(entityHuman);
	}
}

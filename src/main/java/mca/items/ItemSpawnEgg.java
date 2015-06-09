package mca.items;

import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.util.BlockHelper;

public class ItemSpawnEgg extends Item
{
	private boolean isMale;

	public ItemSpawnEgg(boolean isMale)
	{
		final String itemName = isMale ? "EggMale" : "EggFemale";

		this.isMale = isMale;
		this.setCreativeTab(MCA.getCreativeTabMain());
		this.setMaxStackSize(1);
		this.setUnlocalizedName(itemName);

		GameRegistry.registerItem(this, itemName);
	}

	@Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		int posX = pos.getX();
		int posY = pos.getY() + 1;
		int posZ = pos.getZ();
		
		if (!world.isRemote)
		{
			final Block block = BlockHelper.getBlock(world, posX, posY, posZ);
			double verticalOffset = 0.0D;


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

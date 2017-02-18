package mca.items;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
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
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World worldObj, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		int posX = pos.getX();
		int posY = pos.getY() + 1;
		int posZ = pos.getZ();
		
		if (!worldObj.isRemote)
		{
			final Block block = RadixBlocks.getBlock(worldObj, posX, posY, posZ);
			double verticalOffset = 0.0D;

			spawnHuman(worldObj, posX + 0.5D, posY + verticalOffset, posZ + 0.5D);
			
			if (!player.capabilities.isCreativeMode)
			{
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}
		}
		
		return EnumActionResult.PASS;
	}

	public void spawnHuman(World world, double posX, double posY, double posZ)
	{
		EntityVillagerMCA entityHuman = new EntityVillagerMCA(world, isMale);
		entityHuman.setPosition(posX, posY, posZ);
		world.spawnEntityInWorld(entityHuman);
	}
}

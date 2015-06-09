package mca.items;

import mca.blocks.BlockTombstone;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.util.BlockHelper;

public class ItemTombstone extends Item
{
	public ItemTombstone()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(MCA.getCreativeTabMain());
		setUnlocalizedName("tombstone");
		GameRegistry.registerItem(this, "tombstone");
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World worldObj, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (side != EnumFacing.UP)
		{
			return false;
		}

		else
		{
			pos = pos.offset(side);

			if (!ModBlocks.tombstone.canPlaceBlockAt(worldObj, pos))
			{
				return false;
			}

			else
			{
				int i = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
				worldObj.setBlockState(pos, ModBlocks.tombstone.getDefaultState().withProperty(BlockTombstone.ROTATION_PROP, Integer.valueOf(i)), 3);
			}
			
			--stack.stackSize;
			final TileTombstone tombstone = (TileTombstone) BlockHelper.getTileEntity(worldObj, pos.getX(), pos.getY(), pos.getZ());
			
			if (tombstone != null)
			{
				player.openGui(MCA.getInstance(), Constants.GUI_ID_TOMBSTONE, worldObj, pos.getX(), pos.getY(), pos.getZ());
			}

			return true;
		}
	}
}

package mca.items;

import mca.blocks.BlockTombstone;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemTombstone extends Item
{
	public ItemTombstone()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(MCA.getCreativeTabMain());
		setUnlocalizedName("ItemTombstone");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldObj, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		
		if (side != EnumFacing.UP)
		{
			return EnumActionResult.PASS;
		}

		else
		{
			pos = pos.offset(side);

			if (!BlocksMCA.tombstone.canPlaceBlockAt(worldObj, pos))
			{
				return EnumActionResult.FAIL;
			}

			else
			{
				int i = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
				worldObj.setBlockState(pos, BlocksMCA.tombstone.getDefaultState().withProperty(BlockTombstone.ROTATION, Integer.valueOf(i)), 3);
			}
			
			stack.func_190917_f(-1); //Decrease stack size by one
			final TileTombstone tombstone = (TileTombstone) worldObj.getTileEntity(pos);
			
			if (tombstone != null)
			{
				tombstone.setPlayer(player);
				player.openGui(MCA.getInstance(), Constants.GUI_ID_TOMBSTONE, worldObj, pos.getX(), pos.getY(), pos.getZ());
			}

			return EnumActionResult.SUCCESS;
		}
	}
}

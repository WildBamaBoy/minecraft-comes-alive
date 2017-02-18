package mca.items;

import mca.blocks.BlockTombstone;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.modules.RadixBlocks;

public class ItemTombstone extends Item
{
	public ItemTombstone()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(MCA.getCreativeTabMain());
		setUnlocalizedName("ItemTombstone");
		GameRegistry.registerItem(this, "ItemTombstone");
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World worldObj, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (side != EnumFacing.UP)
		{
			return EnumActionResult.PASS;
		}

		else
		{
			pos = pos.offset(side);

			if (!ModBlocks.tombstone.canPlaceBlockAt(worldObj, pos))
			{
				return EnumActionResult.FAIL;
			}

			else
			{
				int i = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
				worldObj.setBlockState(pos, ModBlocks.tombstone.getDefaultState().withProperty(BlockTombstone.ROTATION, Integer.valueOf(i)), 3);
			}
			
			--stack.stackSize;
			final TileTombstone tombstone = (TileTombstone) RadixBlocks.getTileEntity(worldObj, pos.getX(), pos.getY(), pos.getZ());
			
			if (tombstone != null)
			{
				tombstone.setPlayer(player);
				player.openGui(MCA.getInstance(), Constants.GUI_ID_TOMBSTONE, worldObj, pos.getX(), pos.getY(), pos.getZ());
			}

			return EnumActionResult.SUCCESS;
		}
	}
}

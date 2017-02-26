package mca.blocks;

import java.util.Random;

import mca.actions.ActionSleep;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBedColor;
import mca.enums.EnumSleepingState;
import mca.tile.TileVillagerBed;
import net.minecraft.block.BlockBed;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockVillagerBed extends BlockBed implements ITileEntityProvider
{
	private EnumBedColor bedColor;

	public BlockVillagerBed(EnumBedColor bedColor)
	{
		super();
		this.bedColor = bedColor;
		this.setUnlocalizedName("BlockVillagerBed" + bedColor.toString());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileVillagerBed();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) 
	{
		return new ItemStack(getItemDropped(null, world.rand, 0));
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		switch (bedColor)
		{
		case BLUE: return ItemsMCA.bedBlue;
		case GREEN: return ItemsMCA.bedGreen;
		case PINK: return ItemsMCA.bedPink;
		case PURPLE: return ItemsMCA.bedPurple;
		case RED: return ItemsMCA.bedRed;
		default:
			return null;
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) 
	{
        if (worldIn.isRemote)
        {
        	playerIn.sendMessage(new TextComponentString("You cannot sleep in a villager's bed."));
        }
        
        return false;
	}
    
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) 
	{
		onBlockExploded(worldIn, pos, null);
	}
	
	@Override
	public void onBlockExploded(World worldIn, BlockPos pos, Explosion explosion) 
	{
		if (!worldIn.isRemote)
		{
			final TileEntity tileEntity = worldIn.getTileEntity(pos);

			if (tileEntity instanceof TileVillagerBed)
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) tileEntity;

				if (villagerBed.getSleepingVillagerId() != Constants.EMPTY_UUID)
				{
					try
					{
						final EntityVillagerMCA entity = (EntityVillagerMCA) MCA.getEntityByUUID(worldIn, villagerBed.getSleepingVillagerId());

						if (entity != null)
						{
							final ActionSleep sleepAI = entity.getBehavior(ActionSleep.class);
							sleepAI.setSleepingState(EnumSleepingState.INTERRUPTED);
						}
					}

					catch (final NullPointerException e)
					{
						//Ignore
					}
				}
			}
		}
	}
}

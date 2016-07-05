package mca.blocks;

import mca.core.minecraft.ModItems;
import mca.tile.TileMemorial;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.util.BlockHelper;

public class BlockMemorial extends BlockContainer
{
	public BlockMemorial()
	{
		super(Material.cloth);
		setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new TileMemorial();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		return null; //No collision.
	}

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
    {
		setBlockBoundsBasedOnState(world, pos);
		return super.getSelectedBoundingBox(world, pos);
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) 
	{
		return null;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState meta) 
	{
		if (!world.isRemote)
		{
			TileMemorial memorial = (TileMemorial) BlockHelper.getTileEntity(world, pos.getX(), pos.getY(), pos.getZ());
			Item memorialItem = null;
			ItemStack memorialStack = null;

			switch (memorial.getType())
			{
			case BROKEN_RING: memorialItem = ModItems.brokenRing; break;
			case DOLL: memorialItem = ModItems.childsDoll; break;
			case TRAIN: memorialItem = ModItems.toyTrain; break;
			}

			if (memorial.getRevivalTicks() == 0) //Will be 1 when removed from a villager revival.
			{
				memorialStack = new ItemStack(memorialItem);
				memorialStack.setTagCompound(new NBTTagCompound());
				memorialStack.getTagCompound().setInteger("relation", memorial.getRelation().getId());
				memorialStack.getTagCompound().setString("ownerName", memorial.getOwnerName());
				memorial.getVillagerSaveData().writeDataToNBT(memorialStack.getTagCompound());
				
				EntityItem drop = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), memorialStack);
				world.spawnEntityInWorld(drop);
			}
			
			super.breakBlock(world, pos, meta);
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer playerIn, BlockPos pos, IBlockState state, TileEntity te)
	{
		//Do nothing to avoid duplication glitch.
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

    @Override
    public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam) 
    {
        super.onBlockEventReceived(worldIn, pos, state, eventID, eventParam);
        System.out.println("A");
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
    }
}

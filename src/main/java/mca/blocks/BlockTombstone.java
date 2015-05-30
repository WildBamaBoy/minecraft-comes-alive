package mca.blocks;

import mca.core.minecraft.ModItems;
import mca.tile.TileTombstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.util.BlockHelper;

public class BlockTombstone extends BlockContainer
{
	public BlockTombstone()
	{
		super(Material.rock);
		setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new TileTombstone();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		return null; //No collision.
	}

    @SideOnly(Side.CLIENT)
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
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block blockNeighbor)
	{
		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();
		
		if (!BlockHelper.getBlock(world, posX, posY - 1, posZ).getMaterial().isSolid())
		{
			dropBlockAsItem(world, pos, state, 0);
			BlockHelper.setBlock(world, posX, posY, posZ, Blocks.air);
		}

		super.onNeighborBlockChange(world, pos, state, blockNeighbor);
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state)
	{
		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();
		
		if (!world.isRemote)
		{
			dropBlockAsItem(world, pos, state, 0);
			
			TileTombstone tombstone = (TileTombstone) BlockHelper.getTileEntity(world, posX, posY, posZ);
			
			try
			{
				tombstone.invalidate();
			}
			
			catch (Exception e)
			{
				//Ignore
			}
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer playerIn, BlockPos pos, IBlockState state, TileEntity te)
	{
		//Do nothing to avoid duplication glitch.
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();
		
		return BlockHelper.getBlock(world, posX, posY - 1, posZ).isAir(world, pos) && super.canPlaceBlockAt(world, pos);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) 
	{
		return new ItemStack(ModItems.tombstone);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}
}

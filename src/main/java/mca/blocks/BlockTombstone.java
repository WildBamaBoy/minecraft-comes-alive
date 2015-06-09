package mca.blocks;

import java.util.Random;

import mca.core.minecraft.ModItems;
import mca.tile.TileTombstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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
    public static final PropertyInteger ROTATION_PROP = PropertyInteger.create("rotation", 0, 15);
    
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
	public Item getItemDropped(IBlockState state, Random rand, int fortune) 
	{
		return ModItems.tombstone;
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
		
		return !BlockHelper.getBlock(world, posX, posY - 1, posZ).isAir(world, pos) && super.canPlaceBlockAt(world, pos);
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
	
	@Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(ROTATION_PROP, Integer.valueOf(meta));
    }

	@Override
    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(ROTATION_PROP)).intValue();
    }

	@Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {ROTATION_PROP});
    }

    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, net.minecraft.client.particle.EffectRenderer effectRenderer)
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.EffectRenderer effectRenderer)
    {
        return true;
    }
}

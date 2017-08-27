package mca.blocks;

import java.util.Random;

import mca.core.minecraft.ItemsMCA;
import mca.tile.TileTombstone;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTombstone extends BlockContainer
{
	protected static final AxisAlignedBB SIGN_AABB = new AxisAlignedBB(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
    public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);
    
	public BlockTombstone()
	{
		super(Material.CIRCUITS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, Integer.valueOf(0)));
		this.setHarvestLevel("pickaxe", 1);
		this.setHardness(3.0F);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return SIGN_AABB;
	}

	public AxisAlignedBB getSelectedBoundingBox(IBlockState blockState, World worldIn, BlockPos pos)
	{
		return SIGN_AABB;
	}

	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
	{
		return true;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	/**
	 * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
	 */
	public boolean canSpawnInBlock()
	{
		return true;
	}

	/**
	 * Returns a new instance of a block's tile entity class. Called on placing the block.
	 */
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileTombstone();
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return ItemsMCA.TOMBSTONE;
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
	{
		return new ItemStack(ItemsMCA.TOMBSTONE);
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (worldIn.isRemote)
		{
			return true;
		}

		else
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);
			return tileentity instanceof TileTombstone ? ((TileTombstone)tileentity).executeCommand(playerIn) : false;
		}
	}

	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		return !this.hasInvalidNeighbor(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
	}

	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager)
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
	{
		return true;
	}

    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(ROTATION, Integer.valueOf(rot.rotate(((Integer)state.getValue(ROTATION)).intValue(), 16)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withProperty(ROTATION, Integer.valueOf(mirrorIn.mirrorRotation(((Integer)state.getValue(ROTATION)).intValue(), 16)));
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(ROTATION, Integer.valueOf(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(ROTATION)).intValue();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {ROTATION});
    }
}

package mca.blocks;

import java.util.ArrayList;
import java.util.List;

import mca.core.minecraft.ItemsMCA;
import mca.tile.TileMemorial;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMemorial extends BlockContainer
{
	protected static final AxisAlignedBB SIGN_AABB = new AxisAlignedBB(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
    
	public BlockMemorial()
	{
		super(Material.CLOTH);
		setUnlocalizedName("Memorial");
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return SIGN_AABB;
	}

	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
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
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) 
	{
		return null;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState meta) 
	{
		if (!world.isRemote)
		{
			TileMemorial memorial = (TileMemorial) world.getTileEntity(pos);
			Item memorialItem = null;
			ItemStack memorialStack = null;

			switch (memorial.getType())
			{
			case BROKEN_RING: memorialItem = ItemsMCA.BROKEN_RING; break;
			case DOLL: memorialItem = ItemsMCA.CHILDS_DOLL; break;
			case TRAIN: memorialItem = ItemsMCA.TOY_TRAIN; break;
			}

			if (memorial.getRevivalTicks() == 0) //Will be 1 when removed from a villager revival.
			{
				NBTTagCompound stackNBT = new NBTTagCompound();
				
				memorialStack = new ItemStack(memorialItem);
				stackNBT.setInteger("relation", memorial.getRelation().getId());
				stackNBT.setString("ownerName", memorial.getOwnerName());
				stackNBT.setUniqueId("ownerUUID", memorial.getOwnerUUID());
				memorial.getTransitiveVillagerData().writeToNBT(stackNBT);
				
				memorialStack.setTagCompound(stackNBT);
				
				EntityItem drop = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), memorialStack);
				world.spawnEntity(drop);
			}
			
			super.breakBlock(world, pos, meta);
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

    @Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) 
    {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileMemorial();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) 
	{
		//Empty drops, handled by block broken. 
		return new ArrayList<ItemStack>();
	}	
}

package mca.blocks;

import mca.core.minecraft.ModItems;
import mca.tile.TileTombstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

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
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int posX, int posY, int posZ)
	{
		return null; //No collision.
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int posX, int posY, int posZ)
	{
		setBlockBoundsBasedOnState(world, posX, posY, posZ);
		return super.getSelectedBoundingBoxFromPool(world, posX, posY, posZ);
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int posX, int posY, int posZ, Block blockNeighbor)
	{
		if (!world.getBlock(posX, posY - 1, posZ).getMaterial().isSolid())
		{
			dropBlockAsItem(world, posX, posY, posZ, new ItemStack(ModItems.tombstone, 1));
			world.setBlockToAir(posX, posY, posZ);
		}

		super.onNeighborBlockChange(world, posX, posY, posZ, blockNeighbor);
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int posX, int posY, int posZ, int meta)
	{
		if (!world.isRemote)
		{
			dropBlockAsItem(world, posX, posY, posZ, new ItemStack(ModItems.tombstone, 1));
			
			TileTombstone tombstone = (TileTombstone) world.getTileEntity(posX, posY, posZ);
			
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
	public void harvestBlock(World world, EntityPlayer player, int posX, int posY, int posZ, int meta)
	{
		//Do nothing to avoid duplication glitch.
	}

	@Override
	public void registerBlockIcons(IIconRegister IIconRegister)
	{
		blockIcon = IIconRegister.registerIcon("mca:Tombstone");
	}

	@Override
	public boolean canPlaceBlockAt(World world, int posX, int posY, int posZ)
	{
		return world.getBlock(posX, posY - 1, posZ).isAir(world, posX, posY, posZ) && super.canPlaceBlockAt(world, posX, posY, posZ);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) 
	{
		return new ItemStack(ModItems.tombstone);
	}
	
	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
}

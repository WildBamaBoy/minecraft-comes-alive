/*******************************************************************************
 * BlockTombstone.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.block;

import mca.core.MCA;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * Defines the tombstone block and how it behaves.
 */
public class BlockTombstone extends BlockContainer
{
	/**
	 * Constructor
	 */
	public BlockTombstone()
	{
		super(Material.rock);
		setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown) 
	{
		return new TileEntityTombstone();
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
			dropBlockAsItem(world, posX, posY, posZ, new ItemStack(MCA.getInstance().itemTombstone, 1));
			world.setBlockToAir(posX, posY, posZ);
		}

		super.onNeighborBlockChange(world, posX, posY, posZ, blockNeighbor);
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int posX, int posY, int posZ, int meta)
	{
		if (!world.isRemote)
		{
			dropBlockAsItem(world, posX, posY, posZ, new ItemStack(MCA.getInstance().itemTombstone, 1));
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
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
}

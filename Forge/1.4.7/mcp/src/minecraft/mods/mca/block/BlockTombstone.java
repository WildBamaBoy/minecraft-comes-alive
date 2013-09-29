/*******************************************************************************
 * BlockTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.block;

import java.util.Random;

import javax.swing.Icon;

import mods.mca.core.MCA;
import mods.mca.tileentity.TileEntityTombstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Defines the tombstone block and how it behaves.
 */
public class BlockTombstone extends BlockContainer
{
	/** The tile entity that contains the sign information.*/
	private Class signEntityClass;

	/**
	 * Constructor
	 * 
	 * @param 	id				The block's ID.
	 * @param 	tileEntityClass	The tileEntity containing the information for the sign.
	 */
	public BlockTombstone(int id, Class tileEntityClass)
	{
		super(id, Material.rock);

		signEntityClass = tileEntityClass;
		setBlockBounds(0.5F - 0.40F, 0.0F, 0.5F - 0.40F, 0.5F + 0.40F, 1.0F, 0.5F + 0.40F);
	}
    
	@Override
	public TileEntity createNewTileEntity(World world) 
	{
		return new TileEntityTombstone();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		//Return null for no collision.
		return null;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int x, int y, int z)
	{
		return;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		//Return false since the tombstone is not a cube.
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int idDropped(int x, Random random, int y)
	{
		return MCA.instance.itemTombstone.itemID;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int meta)
	{
		boolean flag = false;

		if (!world.getBlockMaterial(x, y - 1, z).isSolid())
		{
			flag = true;
		}

		if (flag)
		{
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, 0);
		}

		super.onNeighborBlockChange(world, x, y, z, meta);
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta)
	{
		if (!world.getWorldInfo().getGameType().isCreative())
		{
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta)
	{
		//Do nothing to avoid duplication glitch.
		return;
	}

	/**
	 * Returns an instance of the sign entity associated with this block.
	 * 
	 * @return	The tile entity associated with this block.
	 */
	public TileEntity getBlockEntity()
	{
		try
		{
			return (TileEntity)signEntityClass.newInstance();
		}

		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
}

/*******************************************************************************
 * BlockTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.block;

import java.util.Random;

import mca.core.MCA;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

/**
 * Defines the tombstone block and how it behaves.
 */
public class BlockTombstone extends BlockContainer
{
	/**
	 * Constructor
	 * 
	 * @param 	blockId	The block's ID.
	 */
	public BlockTombstone(int blockId)
	{
		super(blockId, Material.rock);
		setBlockBounds(0.5F - 0.40F, 0.0F, 0.5F - 0.40F, 0.5F + 0.40F, 1.0F, 0.5F + 0.40F);
	}

	@Override
	public Icon getIcon(int side, int unknown)
	{
		return Block.planks.getBlockTextureFromSide(side);
	}

	@Override
	public TileEntity createNewTileEntity(World world) 
	{
		return new TileEntityTombstone();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int posX, int posY, int posZ)
	{
		//Return null for no collision.
		return null;
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
		//Return false since the tombstone is not a cube.
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int idDropped(int meta, Random random, int fortune)
	{
		return MCA.getInstance().itemTombstone.itemID;
	}

	@Override
	public void onNeighborBlockChange(World world, int posX, int posY, int posZ, int meta)
	{
		if (!world.getBlockMaterial(posX, posY - 1, posZ).isSolid())
		{
			dropBlockAsItem(world, posX, posY, posZ, world.getBlockMetadata(posX, posY, posZ), 0);
			world.setBlock(posX, posY, posZ, 0);
		}

		super.onNeighborBlockChange(world, posX, posY, posZ, meta);
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int posX, int posY, int posZ, int meta)
	{
		if (!world.getWorldInfo().getGameType().isCreative())
		{
			dropBlockAsItem(world, posX, posY, posZ, world.getBlockMetadata(posX, posY, posZ), 0);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int posX, int posY, int posZ, int meta)
	{
		//Do nothing to avoid duplication glitch.
	}

	public void registerIcons(IconRegister iconRegister)
	{
		blockIcon = iconRegister.registerIcon("mca:Tombstone");
	}
}

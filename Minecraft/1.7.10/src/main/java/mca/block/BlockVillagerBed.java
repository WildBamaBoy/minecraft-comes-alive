/*******************************************************************************
 * BlockVillagerBed.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.block;

import java.util.Random;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.network.packets.PacketSetFieldValue;
import mca.tileentity.TileEntityVillagerBed;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockVillagerBed extends BlockDirectional implements ITileEntityProvider
{
	public static final int[][] blockMap = new int[][] {{0, 1}, { -1, 0}, {0, -1}, {1, 0}};

	@SideOnly(Side.CLIENT)
	protected IIcon[] textureEnd;
	@SideOnly(Side.CLIENT)
	protected IIcon[] textureSide;
	@SideOnly(Side.CLIENT)
	protected IIcon[] textureTop;

	public BlockVillagerBed()
	{
		super(Material.cloth);
		this.setBlockBounds();
	}

	@Override
	public boolean onBlockActivated(World worldObj, int posX, int posY, int posZ, EntityPlayer entityPlayer, int unknown, float unknown2, float unknown3, float unknown4)
	{
		if (worldObj.isRemote)
		{
			entityPlayer.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("notify.player.clickbed")));
		}

		return true;
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int p_149691_1_, int p_149691_2_)
	{
		if (p_149691_1_ == 0)
		{
			return Blocks.planks.getBlockTextureFromSide(p_149691_1_);
		}

		else
		{
			int k = getDirection(p_149691_2_);
			int l = Direction.bedDirection[k][p_149691_1_];
			int i1 = isBlockHeadOfBed(p_149691_2_) ? 1 : 0;
			return (i1 != 1 || l != 2) && (i1 != 0 || l != 3) ? (l != 5 && l != 4 ? this.textureTop[i1] : this.textureSide[i1]) : this.textureEnd[i1];
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public abstract void registerBlockIcons(IIconRegister iconRegister);

	@Override
	public int getRenderType()
	{
		return 14;
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
	public void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_)
	{
		this.setBlockBounds();
	}

	@Override
	public void onNeighborBlockChange(World worldObj, int posX, int posY, int posZ, Block block)
	{
		int l = worldObj.getBlockMetadata(posX, posY, posZ);
		int i1 = getDirection(l);

		if (isBlockHeadOfBed(l))
		{
			if (worldObj.getBlock(posX - blockMap[i1][0], posY, posZ - blockMap[i1][1]) != this)
			{
				worldObj.setBlockToAir(posX, posY, posZ);
			}
		}
		else if (worldObj.getBlock(posX + blockMap[i1][0], posY, posZ + blockMap[i1][1]) != this)
		{
			worldObj.setBlockToAir(posX, posY, posZ);

			if (!worldObj.isRemote)
			{
				this.dropBlockAsItem(worldObj, posX, posY, posZ, l, 0);
			}
		}
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
	{
		return Item.getItemById(0);
		//return isBlockHeadOfBed(p_149650_1_) ? Item.getItemById(0) : MCA.getInstance().itemVillagerBed;
	}

	private void setBlockBounds()
	{
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625F, 1.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown) 
	{
		return new TileEntityVillagerBed();
	}

	public static boolean isBlockHeadOfBed(int meta)
	{
		return (meta & 8) != 0;
	}

	public static boolean isBlockFootOfBed(int meta)
	{
		return (meta & 4) != 0;
	}

	@Override
	public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_)
	{
		if (!isBlockHeadOfBed(p_149690_5_))
		{
			super.dropBlockAsItemWithChance(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_6_, 0);
		}
	}

	@Override
	public int getMobilityFlag()
	{
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_)
	{
		return Items.bed;
	}

	@Override
	public void onBlockHarvested(World world, int posX, int posY, int posZ, int meta, EntityPlayer entityPlayer)
	{
		if (entityPlayer.capabilities.isCreativeMode && isBlockHeadOfBed(meta))
		{
			int direction = getDirection(meta);
			posX -= blockMap[direction][0];
			posZ -= blockMap[direction][1];

			if (world.getBlock(posX, posY, posZ) == this)
			{
				world.setBlockToAir(posX, posY, posZ);
			}
		}
	}


	@Override
	public void onBlockPreDestroy(World world, int posX, int posY, int posZ, int meta) 
	{
		super.onBlockPreDestroy(world, posX, posY, posZ, meta);

		if (!world.isRemote)
		{
			final TileEntity tileEntity = world.getTileEntity(posX, posY, posZ);

			if (tileEntity instanceof TileEntityVillagerBed)
			{
				TileEntityVillagerBed villagerBed = (TileEntityVillagerBed) tileEntity;

				if (villagerBed.getSleepingVillagerId() != -1)
				{
					Entity entity = world.getEntityByID(MCA.getInstance().idsMap.get(villagerBed.getSleepingVillagerId()));

					if (entity != null)
					{
						AbstractEntity villager = (AbstractEntity)entity;

						villager.isSleeping = false;
						villager.resetBedStatus();
						villager.texture = villager.texture.replace("/skins/sleeping/", "/skins/");

						MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(villager.getEntityId(), "texture", villager.texture));
						MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(villager.getEntityId(), "isSleeping", villager.isSleeping));
					}
				}
			}
		}
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
}

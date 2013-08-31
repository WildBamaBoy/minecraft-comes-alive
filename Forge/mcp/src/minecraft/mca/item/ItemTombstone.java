/*******************************************************************************
 * ItemTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.MCA;
import mca.core.util.LanguageHelper;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the Tombstone is and how it behaves.
 */
public class ItemTombstone extends Item
{	
	/**
	 * Constructor
	 *
	 * @param	id	The Item's ID.
	 */
	public ItemTombstone(int id)
	{
		super(id);
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.tabMisc);
	}

	/**
	 * Called when the player right clicks a block while holding this item.
	 * 
	 * @param	itemStack	The item stack that the player was holding when they right clicked.
	 * @param	player		The player that right clicked.
	 * @param	world		The world that the player right clicked in.
	 * @param	x			X coordinate of the block that the player right clicked.
	 * @param	y			Y coordinate of the block that the player right clicked.
	 * @param	z			Z coordinate of the block that the player right clicked.
	 * @param	meta		Metadata associated with the block clicked.
	 * @param	xOffset		X offset of the point where the block was clicked.
	 * @param	yOffset		Y offset of the point where the block was clicked.
	 * @param	zOffset		Z offset of the point where the block was clicked.
	 * 
	 * @return	True or false depending on if placing the item into the world was successful.
	 */
	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (meta == 0)
		{
			return false;
		}

		else if (!world.getBlockMaterial(x, y, z).isSolid())
		{
			return false;
		}

		else
		{
			if (meta == 1)
			{
				++y;
			}

			if (meta == 2)
			{
				--z;
			}

			if (meta == 3)
			{
				++z;
			}

			if (meta == 4)
			{
				--x;
			}

			if (meta == 5)
			{
				++x;
			}

			if (!player.canPlayerEdit(x, y, z, meta, itemStack))
			{
				return false;
			}

			else if (!MCA.instance.blockTombstone.canPlaceBlockAt(world, x, y, z))
			{
				return false;
			}

			else
			{
				if (meta == 1)
				{
					int i1 = MathHelper.floor_double(((player.rotationYaw + 180F) * 16F) / 360F + 0.5D) & 0xf;

					if (i1 == 0 || i1 == 4 || i1 == 8 || i1 == 12)
					{
						world.setBlock(x, y, z, MCA.instance.blockTombstone.blockID, i1, 2);
					}
					else
					{
						return false;
					}
				}
				else
				{
					world.setBlock(x, y, z, MCA.instance.blockTombstone.blockID, meta, 2);
				}

				--itemStack.stackSize;
				TileEntityTombstone tombstone = (TileEntityTombstone)world.getBlockTileEntity(x, y, z);

				if (tombstone != null)
				{
					player.openGui(MCA.instance, MCA.instance.guiTombstoneID, world, tombstone.xCoord, tombstone.yCoord, tombstone.zCoord);
				}

				return true;
			}
		}
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		itemIcon = iconRegister.registerIcon("mca:Tombstone");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.tombstone.line1"));
		informationList.add(LanguageHelper.getString("information.tombstone.line2"));
	}
}

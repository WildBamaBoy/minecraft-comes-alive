/*******************************************************************************
 * ItemTombstone.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.client.renderer.texture.IIconRegister;
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
	 */
	public ItemTombstone()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.tabMisc);
	}

	/**
	 * Called when the player right clicks a block while holding this item.
	 * 
	 * @param	itemStack	The item stack that the player was holding when they right clicked.
	 * @param	player		The player that right clicked.
	 * @param	world		The world that the player right clicked in.
	 * @param	posX		X coordinate of the block that the player right clicked.
	 * @param	posY		Y coordinate of the block that the player right clicked.
	 * @param	posZ		Z coordinate of the block that the player right clicked.
	 * @param	meta		Metadata associated with the block clicked.
	 * @param	xOffset		X offset of the point where the block was clicked.
	 * @param	yOffset		Y offset of the point where the block was clicked.
	 * @param	zOffset		Z offset of the point where the block was clicked.
	 * 
	 * @return	True or false depending on if placing the item into the world was successful.
	 */
	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (meta == 0)
		{
			return false;
		}

		else if (!world.getBlock(posX, posY, posZ).getMaterial().isSolid())
		{
			return false;
		}

		else
		{
			if (meta == 1)
			{
				++posY;
			}

			if (meta == 2)
			{
				--posZ;
			}

			if (meta == 3)
			{
				++posZ;
			}

			if (meta == 4)
			{
				--posX;
			}

			if (meta == 5)
			{
				++posX;
			}

			if (!player.canPlayerEdit(posX, posY, posZ, meta, itemStack))
			{
				return false;
			}

			else if (!MCA.getInstance().blockTombstone.canPlaceBlockAt(world, posX, posY, posZ) && !world.isAirBlock(posX, posY, posZ))
			{
				return false;
			}

			else
			{
				if (meta == 1)
				{
					final int newMeta = MathHelper.floor_double(((player.rotationYaw + 180F) * 16F) / 360F + 0.5D) & 15;
					world.setBlock(posX, posY, posZ, MCA.getInstance().blockTombstone, newMeta, 2);
				}
				
				else
				{
					world.setBlock(posX, posY, posZ, MCA.getInstance().blockTombstone, meta, 2);
				}

				--itemStack.stackSize;
				final TileEntityTombstone tombstone = (TileEntityTombstone)world.getTileEntity(posX, posY, posZ);
				
				if (tombstone != null)
				{
					player.openGui(MCA.getInstance(), Constants.ID_GUI_TOMBSTONE, world, tombstone.xCoord, tombstone.yCoord, tombstone.zCoord);
					//MCA.getInstance().getPacketPipeline().sendPacketToPlayer(new Packet(EnumPacketType.OpenGui, player.getEntityId(), Constants.ID_GUI_TOMBSTONE), (EntityPlayerMP)player);
				}
				
				return true;
			}
		}
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:Tombstone");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.tombstone.line1"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.tombstone.line2"));
	}
}

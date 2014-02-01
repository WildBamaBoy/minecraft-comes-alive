/*******************************************************************************
 * ItemWhistle.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.util.LanguageHelper;
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityVillagerAdult;
import mca.enums.EnumGenericCommand;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the Whistle is and how it behaves.
 */
public class ItemWhistle extends Item
{
	/**
	 * Constructor
	 * 
	 * @param 	id	The item's ID.
	 */
	public ItemWhistle(int itemId)
	{
		super(itemId);
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.tabMisc);
	}

	/**
	 * Called when the player right clicks the ground with the item equipped.
	 * 
	 * @param	itemStack	The item stack the player was holding.
	 * @param	world		The world that the player was in.
	 * @param	player		The player that right clicked.
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			for (final Object obj : world.loadedEntityList)
			{
				if (obj instanceof AbstractChild)
				{
					final AbstractChild child = (AbstractChild)obj;
					
					if (child.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
					{
						child.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
					}
				}

				else if (obj instanceof EntityVillagerAdult)
				{
					final EntityVillagerAdult adult = (EntityVillagerAdult)obj;

					if (adult.isMarriedToPlayer || adult.isEngaged && adult.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
					{
						adult.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
					}
				}
			}
		}

		return itemStack;
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		itemIcon = iconRegister.registerIcon("mca:Whistle");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.whistle.line1"));
		informationList.add(LanguageHelper.getString("information.whistle.line2"));
		informationList.add(LanguageHelper.getString("information.whistle.line3"));
	}
}

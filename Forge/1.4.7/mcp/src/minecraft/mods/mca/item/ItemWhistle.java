/*******************************************************************************
 * ItemWhistle.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.item;

import java.util.List;

import mods.mca.core.MCA;
import mods.mca.core.forge.ClientProxy;
import mods.mca.core.util.LanguageHelper;
import mods.mca.entity.AbstractEntity;
import mods.mca.entity.EntityChild;
import mods.mca.entity.EntityVillagerAdult;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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
	public ItemWhistle(int id)
	{
		super(id);
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.tabMisc);
		setIconIndex(7);
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
			for (AbstractEntity entity : MCA.instance.entitiesMap.values())
			{
				if (entity instanceof EntityChild)
				{
					if (entity.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
					{
						entity.setPosition(player.posX, player.posY, player.posZ);
					}
				}

				else if (entity instanceof EntityVillagerAdult)
				{
					EntityVillagerAdult adult = (EntityVillagerAdult)entity;
					if (adult.isSpouse || adult.isEngaged)
					{
						if (entity.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
						{
							entity.setPosition(player.posX, player.posY, player.posZ);
						}
					}
				}
			}
		}

		return itemStack;
	}

    @SideOnly(Side.CLIENT)
    @Override
    public String getTextureFile()
    {
    	return ClientProxy.items;
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

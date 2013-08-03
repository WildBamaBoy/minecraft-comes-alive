/*******************************************************************************
 * ItemBaby.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the ItemBaby is and information about it. This serves as a base class to the ItemBabyBoy and ItemBabyGirl.
 */
public class ItemBaby extends Item
{
	/** The gender of the baby. */
	public String gender;

	/**
	 * Constructor
	 *
	 * @param	id	The item's ID.
	 */
	public ItemBaby(int id)
	{
		super(id);
		maxStackSize = 1;
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
		WorldPropertiesManager worldPropertiesManager = MCA.instance.playerWorldManagerMap.get(player.username);

		if (worldPropertiesManager.worldProperties.babyReadyToGrow)
		{
			if (!world.isRemote)
			{	
				EntityPlayerChild entityPlayerChild = new EntityPlayerChild(world, player, worldPropertiesManager.worldProperties.babyName, gender);
				entityPlayerChild.setLocationAndAngles(x, y + 1, z, player.rotationYaw, player.rotationPitch);
				world.spawnEntityInWorld(entityPlayerChild);

				//Trigger the achievement
				player.triggerAchievement(MCA.instance.achievementBabyGrowUp);

				WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);

				//Set relevant properties back to their default values so that the player can have another baby.
				manager.worldProperties.babyExists = false;
				manager.worldProperties.babyName = "";
				manager.worldProperties.babyReadyToGrow = false;
				manager.worldProperties.babyGender = "";
				manager.worldProperties.minutesBabyExisted = 0;
				manager.saveWorldProperties();

				//Check if married to another player.
				if (manager.worldProperties.playerSpouseID < 0)
				{
					int spouseID = manager.worldProperties.playerSpouseID;
					EntityPlayer spouseEntity = MCA.instance.getPlayerByID(world, spouseID);
					WorldPropertiesManager spouseManager = null;
					
					if (spouseEntity != null)
					{
						spouseManager = MCA.instance.playerWorldManagerMap.get(spouseEntity.username);
					}
					
					//Fail-safe for when spouse is not logged in. Their properties are still loaded, though.
					else
					{
						spouseManager = MCA.instance.playerWorldManagerMap.get(manager.worldProperties.playerSpouseName);
					}
					
					spouseManager.worldProperties.babyExists = false;
					spouseManager.worldProperties.babyName = "";
					spouseManager.worldProperties.babyReadyToGrow = false;
					spouseManager.worldProperties.babyGender = "";
					spouseManager.worldProperties.minutesBabyExisted = 0;
					spouseManager.saveWorldProperties();
				}

				MCA.instance.hasNotifiedOfBabyReadyToGrow = false;
			}

			else
			{
				AbstractEntity.removeItemFromPlayer(itemStack, player);
			}
		}

		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.baby.line1"));
		informationList.add(LanguageHelper.getString("information.baby.line2"));
		informationList.add(LanguageHelper.getString("information.baby.line3"));
	}
}

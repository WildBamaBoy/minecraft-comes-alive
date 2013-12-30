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
	public boolean isMale;

	/**
	 * Constructor
	 *
	 * @param	id	The item's ID.
	 */
	public ItemBaby(int itemId)
	{
		super(itemId);
		maxStackSize = 1;
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

		if (!world.isRemote)
		{	
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

			if (manager.worldProperties.babyReadyToGrow)
			{
				final EntityPlayerChild entityPlayerChild = new EntityPlayerChild(world, player, manager.worldProperties.babyName, isMale);
				entityPlayerChild.setLocationAndAngles(posX, posY + 1, posZ, player.rotationYaw, player.rotationPitch);
				world.spawnEntityInWorld(entityPlayerChild);

				//Trigger the achievement
				player.triggerAchievement(MCA.getInstance().achievementBabyGrowUp);

				//Set relevant properties back to their default values so that the player can have another baby.
				manager.worldProperties.babyExists = false;
				manager.worldProperties.babyName = "";
				manager.worldProperties.babyReadyToGrow = false;
				manager.worldProperties.babyIsMale = false;
				manager.worldProperties.minutesBabyExisted = 0;
				manager.saveWorldProperties();

				//Check if married to another player.
				if (manager.worldProperties.playerSpouseID < 0)
				{
					final int spouseID = manager.worldProperties.playerSpouseID;
					final EntityPlayer spouseEntity = MCA.getInstance().getPlayerByID(world, spouseID);
					WorldPropertiesManager spouseManager = null;

					if (spouseEntity == null)
					{
						//Properties for player will still be loaded when they are not logged in.
						spouseManager = MCA.getInstance().playerWorldManagerMap.get(manager.worldProperties.playerSpouseName);						
					}

					else
					{
						spouseManager = MCA.getInstance().playerWorldManagerMap.get(spouseEntity.username);
					}

					spouseManager.worldProperties.babyExists = false;
					spouseManager.worldProperties.babyName = "";
					spouseManager.worldProperties.babyReadyToGrow = false;
					spouseManager.worldProperties.babyIsMale = false;
					spouseManager.worldProperties.minutesBabyExisted = 0;
					spouseManager.saveWorldProperties();
				}

				MCA.getInstance().hasNotifiedOfBabyReadyToGrow = false;
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

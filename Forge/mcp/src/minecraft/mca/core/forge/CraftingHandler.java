/*******************************************************************************
 * CraftingHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.PacketHelper;
import mca.item.ItemBaby;
import mca.item.ItemCrown;
import mca.item.ItemFertilityPotion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * Handles onCrafting and onSmelting events in MCA. Client and server side.
 */
public class CraftingHandler implements ICraftingHandler
{
	@Override
	public void onCrafting(EntityPlayer player, ItemStack itemStack, IInventory craftMatrix) 
	{
		if (itemStack.getItem() instanceof ItemCrown)
		{
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
			
			if (!manager.worldProperties.isMonarch)
			{
				manager.worldProperties.isMonarch = true;
				manager.saveWorldProperties();
				
				PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementCraftCrown, player.entityId));
				player.triggerAchievement(MCA.instance.achievementCraftCrown);
				
				if (player.worldObj.isRemote)
				{
					player.addChatMessage(LanguageHelper.getString("notify.monarch.began"));
				}
			}
		}
		
		else if (itemStack.getItem() instanceof ItemFertilityPotion)
		{
			player.triggerAchievement(MCA.instance.achievementMakeFertilityPotion);
		}
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack itemStack) 
	{
		if (itemStack.getItem() instanceof ItemBaby)
		{
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
			
			//Reset all information about the baby.
			manager.worldProperties.babyExists = false;
			manager.worldProperties.babyGender = "";
			manager.worldProperties.babyName = "";
			manager.worldProperties.babyReadyToGrow = false;
			manager.worldProperties.minutesBabyExisted = 0;
			
			manager.saveWorldProperties();
			
			if (player.worldObj.isRemote)
			{
				player.addChatMessage(LanguageHelper.getString("notify.baby.cooked"));
			}
			
			PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementCookBaby, player.entityId));
			player.triggerAchievement(MCA.instance.achievementCookBaby);
		}
	}
}

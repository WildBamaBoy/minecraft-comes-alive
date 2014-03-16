/*******************************************************************************
 * CraftingHandler.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.item.ItemCrown;
import mca.item.ItemTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Handles onCrafting and onSmelting events in MCA. Client and server side.
 */
public class CraftingHandler implements ICraftingHandler
{
	@Override
	public void onCrafting(EntityPlayer player, ItemStack itemStack, IInventory craftMatrix) 
	{
		if (itemStack.getItem() instanceof ItemCrown && !player.worldObj.isRemote)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

			if (!manager.worldProperties.isMonarch)
			{
				manager.worldProperties.isMonarch = true;
				manager.saveWorldProperties();

				PacketDispatcher.sendPacketToPlayer(PacketHandler.createSayLocalizedPacket(player, null, "notify.monarch.began", false, null, null), (Player)player);
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementCraftCrown, player.entityId), (Player)player);
				player.triggerAchievement(MCA.getInstance().achievementCraftCrown);
			}
		}
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack itemStack) 
	{
		if (itemStack.getItem() instanceof ItemTombstone && !player.worldObj.isRemote)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

			//Reset all information about the baby.
			manager.worldProperties.babyExists = false;
			manager.worldProperties.babyIsMale = false;
			manager.worldProperties.babyName = "";
			manager.worldProperties.babyReadyToGrow = false;
			manager.worldProperties.minutesBabyExisted = 0;

			manager.saveWorldProperties();

			PacketDispatcher.sendPacketToPlayer(PacketHandler.createSayLocalizedPacket(player, null, "notify.baby.cooked", false, null, null), (Player)player);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementCookBaby, player.entityId), (Player)player);
			player.triggerAchievement(MCA.getInstance().achievementCookBaby);
		}
	}
}

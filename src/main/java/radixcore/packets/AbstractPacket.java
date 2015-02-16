/*******************************************************************************
 * AbstractPacket.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package radixcore.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractPacket
{
	public EntityPlayer getPlayer(MessageContext context)
	{
		EntityPlayer player = null;

		if (context.side == Side.CLIENT)
		{
			return getPlayerClient();
		}

		else
		{
			player = context.getServerHandler().playerEntity;
		}

		return player;
	}

	@SideOnly(Side.CLIENT)
	public EntityPlayer getPlayerClient()
	{
		return Minecraft.getMinecraft().thePlayer;
	}
}

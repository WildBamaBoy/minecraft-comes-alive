/*******************************************************************************
 * PacketNotifyLocalized.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketNotifyLocalized extends AbstractPacket implements IMessage, IMessageHandler<PacketNotifyLocalized, IMessage>
{
	private String phraseId;
	private int parsePlayerId;
	
	public PacketNotifyLocalized()
	{
	}
	
	public PacketNotifyLocalized(String phraseId)
	{
		this.phraseId = phraseId;
		this.parsePlayerId = 0;
	}
	
	public PacketNotifyLocalized(String phraseId, EntityPlayer parsePlayer)
	{
		this.phraseId = phraseId;
		this.parsePlayerId = parsePlayer.getEntityId();
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		phraseId = (String) ByteBufIO.readObject(buf);
		parsePlayerId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufIO.writeObject(buf, phraseId);
		buf.writeInt(parsePlayerId);
	}
	
	@Override
	public IMessage onMessage(PacketNotifyLocalized message, MessageContext ctx)
	{
		final EntityPlayer player = this.getPlayerClient();
		final EntityPlayer parsePlayer = message.parsePlayerId == 0 ? null : (EntityPlayer) player.worldObj.getEntityByID(message.parsePlayerId);
		
		final String spokenString = MCA.getInstance().getLanguageLoader().getString(message.phraseId, parsePlayer);
		
		player.addChatComponentMessage(new ChatComponentText(spokenString));
		
		return null;
	}	
}

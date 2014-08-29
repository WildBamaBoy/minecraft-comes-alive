/*******************************************************************************
 * PacketSetTombstoneText.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSetTombstoneText extends AbstractPacket implements IMessage, IMessageHandler<PacketSetTombstoneText, IMessage>
{
	private int posX;
	private int posY;
	private int posZ;
	private String line1;
	private String line2;
	private String line3;
	private String line4;

	public PacketSetTombstoneText()
	{
	}

	public PacketSetTombstoneText(int posX, int posY, int posZ, String line1, String line2, String line3, String line4)
	{
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.line4 = line4;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		posX = byteBuf.readInt();
		posY = byteBuf.readInt();
		posZ = byteBuf.readInt();
		line1 = (String) ByteBufIO.readObject(byteBuf);
		line2 = (String) ByteBufIO.readObject(byteBuf);
		line3 = (String) ByteBufIO.readObject(byteBuf);
		line4 = (String) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(posX);
		byteBuf.writeInt(posY);
		byteBuf.writeInt(posZ);
		ByteBufIO.writeObject(byteBuf, line1);
		ByteBufIO.writeObject(byteBuf, line2);
		ByteBufIO.writeObject(byteBuf, line3);
		ByteBufIO.writeObject(byteBuf, line4);
	}

	@Override
	public IMessage onMessage(PacketSetTombstoneText packet, MessageContext context)
	{
		final EntityPlayer player = getPlayer(context);
		final TileEntityTombstone tombstone = (TileEntityTombstone) player.worldObj.getTileEntity(packet.posX, packet.posY, packet.posZ);

		if (tombstone != null)
		{
			tombstone.signText[0] = packet.line1;
			tombstone.signText[1] = packet.line2;
			tombstone.signText[2] = packet.line3;
			tombstone.signText[3] = packet.line4;
			tombstone.markDirty();
		}

		return null;
	}
}

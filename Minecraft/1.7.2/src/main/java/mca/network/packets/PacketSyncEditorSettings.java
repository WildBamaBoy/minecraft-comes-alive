/*******************************************************************************
 * PacketSyncEditorSettings.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.inventory.Inventory;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncEditorSettings extends AbstractPacket implements IMessage, IMessageHandler<PacketSyncEditorSettings, IMessage>
{
	private int entityId;
	private String name;
	private boolean isMale;
	private int profession;
	private float moodPointsAnger;
	private float moodPointsHappy;
	private float moodPointsSad;
	private int traitId;
	private Inventory inventory;
	private String texture;

	public PacketSyncEditorSettings()
	{
	}

	public PacketSyncEditorSettings(AbstractEntity entity)
	{
		entityId = entity.getEntityId();
		name = entity.name;
		isMale = entity.isMale;
		profession = entity.profession;
		moodPointsAnger = entity.moodPointsAnger;
		moodPointsHappy = entity.moodPointsHappy;
		moodPointsSad = entity.moodPointsSad;
		traitId = entity.traitId;
		inventory = entity.inventory;
		texture = entity.getTexture();
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		entityId = byteBuf.readInt();
		name = (String) ByteBufIO.readObject(byteBuf);
		isMale = byteBuf.readBoolean();
		profession = byteBuf.readInt();
		moodPointsAnger = byteBuf.readFloat();
		moodPointsHappy = byteBuf.readFloat();
		moodPointsSad = byteBuf.readFloat();
		traitId = byteBuf.readInt();
		inventory = (Inventory) ByteBufIO.readObject(byteBuf);
		texture = (String) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, name);
		byteBuf.writeBoolean(isMale);
		byteBuf.writeInt(profession);
		byteBuf.writeFloat(moodPointsAnger);
		byteBuf.writeFloat(moodPointsHappy);
		byteBuf.writeFloat(moodPointsSad);
		byteBuf.writeInt(traitId);
		ByteBufIO.writeObject(byteBuf, inventory);
		ByteBufIO.writeObject(byteBuf, texture);
	}

	@Override
	public IMessage onMessage(PacketSyncEditorSettings packet, MessageContext context)
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.entityId);

		if (player != null && entity != null)
		{
			entity.name = packet.name;
			entity.isMale = packet.isMale;
			entity.profession = packet.profession;
			entity.moodPointsAnger = packet.moodPointsAnger;
			entity.moodPointsHappy = packet.moodPointsHappy;
			entity.moodPointsSad = packet.moodPointsSad;
			entity.traitId = packet.traitId;
			entity.inventory = packet.inventory;
			entity.texture = packet.texture;

			MCA.packetHandler.sendPacketToAllPlayers(new PacketSync(entity.getEntityId(), entity));
		}

		return null;
	}
}

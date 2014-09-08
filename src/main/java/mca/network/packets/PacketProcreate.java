/*******************************************************************************
 * PacketProcreate.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketProcreate extends AbstractPacket implements IMessage, IMessageHandler<PacketProcreate, IMessage>
{
	private int instructionId;
	private int mcaEntityId;

	public PacketProcreate()
	{
	}

	public PacketProcreate(int instructionId, int mcaEntityId)
	{
		this.instructionId = instructionId;
		this.mcaEntityId = mcaEntityId;
	}

	@Override
	public IMessage onMessage(PacketProcreate message, MessageContext ctx)
	{
		switch (message.instructionId)
		{
			case TypeIDs.Procreation.START: 		handleStartProcreation(message, ctx); break;
			case TypeIDs.Procreation.STOP:  		handleStopProcreation(message, ctx);  break;
			case TypeIDs.Procreation.START_CLIENT: 	handleStartClient(message, ctx);		 break;
		}

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		instructionId = buf.readInt();
		mcaEntityId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(instructionId);
		buf.writeInt(mcaEntityId);
	}

	private void handleStartProcreation(PacketProcreate message, MessageContext ctx) //Server-side only
	{
		EntityPlayer player = getPlayer(ctx);
		World worldObj = player.worldObj;
		AbstractEntity entity = (AbstractEntity) worldObj.getEntityByID(message.mcaEntityId);

		//Start the procreation loop.
		entity.spousePlayerName = player.getCommandSenderName(); //May not be assigned, on occasion.
		entity.isProcreatingWithPlayer = true;
		entity.procreateTicks = 0;
		entity.isSleeping = false;

		//Tell client to start its loop for all players.
		MCA.packetHandler.sendPacketToAllPlayers(new PacketProcreate(TypeIDs.Procreation.START_CLIENT, message.mcaEntityId));
	}

	private void handleStopProcreation(PacketProcreate message, MessageContext ctx) //Client-side only
	{
		EntityPlayer player = getPlayer(ctx);
		World worldObj = Minecraft.getMinecraft().theWorld;
		AbstractEntity entity = (AbstractEntity) worldObj.getEntityByID(message.mcaEntityId);

		entity.isSleeping = false;
		entity.isProcreatingWithPlayer = false;
		entity.setJumping(false);
		
		if (entity.spousePlayerName.equals(player.getCommandSenderName()))
		{
			entity.faceEntity(player, 360F, 360F);
			entity.rotationYawHead = entity.rotationYaw;
			entity.rotationPitch = 0;
		}
	}

	private void handleStartClient(PacketProcreate message, MessageContext ctx)
	{
		World worldObj = Minecraft.getMinecraft().theWorld;
		AbstractEntity entity = (AbstractEntity) worldObj.getEntityByID(message.mcaEntityId);

		entity.isSleeping = false;
		entity.isProcreatingWithPlayer = true;
		entity.setJumping(true);
	}
}
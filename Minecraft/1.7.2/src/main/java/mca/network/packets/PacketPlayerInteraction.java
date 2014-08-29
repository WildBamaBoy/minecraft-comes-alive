/*******************************************************************************
 * PacketPlayerInteraction.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.WorldPropertiesList;
import mca.core.util.ServerLimits;
import mca.core.util.Utility;
import mca.core.util.object.PlayerInfo;
import mca.item.AbstractBaby;
import mca.item.ItemWeddingRing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketPlayerInteraction extends AbstractPacket implements IMessage, IMessageHandler<PacketPlayerInteraction, IMessage>
{
	private int interactionId;
	private String senderPlayerName;
	private String targetPlayerName;

	public PacketPlayerInteraction()
	{

	}

	public PacketPlayerInteraction(int interactionId, String senderPlayerName, String targetPlayerName)
	{
		this.interactionId = interactionId;
		this.senderPlayerName = senderPlayerName;
		this.targetPlayerName = targetPlayerName;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.interactionId = buf.readInt();
		this.senderPlayerName = (String)ByteBufIO.readObject(buf);
		this.targetPlayerName = (String)ByteBufIO.readObject(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(interactionId);
		ByteBufIO.writeObject(buf, senderPlayerName);
		ByteBufIO.writeObject(buf, targetPlayerName);
	}

	@Override
	public IMessage onMessage(PacketPlayerInteraction message, MessageContext ctx)
	{
		PlayerInfo senderInfo = new PlayerInfo(ctx.getServerHandler().playerEntity.worldObj, message.senderPlayerName);
		PlayerInfo targetInfo = new PlayerInfo(ctx.getServerHandler().playerEntity.worldObj, message.targetPlayerName);

		if (!senderInfo.isBad() && !targetInfo.isBad())
		{
			switch (message.interactionId)
			{
				case 1: onAskToMarry(senderInfo, targetInfo); break;
				case 2: onAcceptMarriage(senderInfo, targetInfo); break;
				case 3: onDeclineMarriage(senderInfo, targetInfo); break;
				case 4: onHaveBaby(senderInfo, targetInfo); break;
				case 5: onAcceptBaby(senderInfo, targetInfo); break;
				case 6: onDeclineBaby(senderInfo, targetInfo); break;
				case 7: onDivorce(senderInfo, targetInfo); break;
			}
		}

		return null;
	}

	private void onAskToMarry(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		boolean senderIsMarried = senderInfo.getPropertiesList().playerSpouseID != 0;
		boolean targetIsMarried = targetInfo.getPropertiesList().playerSpouseID != 0;
		boolean senderHasWeddingRing = doesPlayerHaveWeddingRing(senderInfo.getPlayer());

		if (senderIsMarried)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.marry.senderalreadymarried"), (EntityPlayerMP) senderInfo.getPlayer());
		}

		else if (targetIsMarried)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.marry.otheralreadymarried"), (EntityPlayerMP) senderInfo.getPlayer());
		}

		else if (!senderHasWeddingRing)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.marry.noring"), (EntityPlayerMP) senderInfo.getPlayer());
		}

		else
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(senderInfo.getPlayer().getEntityId(), Constants.ID_GUI_MARRYREQUEST), (EntityPlayerMP)targetInfo.getPlayer());
		}
	}

	private void onAcceptMarriage(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		targetInfo.getPlayer().inventory.consumeInventoryItem(MCA.getInstance().itemWeddingRing);

		WorldPropertiesList senderList = senderInfo.getPropertiesList();
		WorldPropertiesList targetList = targetInfo.getPropertiesList();

		senderList.playerSpouseName = targetInfo.getPlayer().getCommandSenderName();
		senderList.playerSpouseID = targetList.playerID;

		targetList.playerSpouseName = senderInfo.getPlayer().getCommandSenderName();
		targetList.playerSpouseID = senderList.playerID;

		senderInfo.getManager().saveWorldProperties();
		targetInfo.getManager().saveWorldProperties();

		MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.marry.accept", targetInfo.getPlayer()), (EntityPlayerMP) senderInfo.getPlayer());
		MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.marry.accept", senderInfo.getPlayer()), (EntityPlayerMP) targetInfo.getPlayer());
	}

	private void onDeclineMarriage(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.marry.decline", senderInfo.getPlayer()), (EntityPlayerMP) targetInfo.getPlayer());
	}

	private void onHaveBaby(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		if (senderInfo.getPropertiesList().babyExists && targetInfo.getPropertiesList().babyExists)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("notify.baby.exists"), (EntityPlayerMP) senderInfo.getPlayer());
		}

		else if (ServerLimits.hasPlayerReachedBabyLimit(senderInfo.getPlayer()) || ServerLimits.hasPlayerReachedBabyLimit(targetInfo.getPlayer()))
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.havebaby.limitreached"), (EntityPlayerMP) senderInfo.getPlayer());
		}

		else
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(senderInfo.getPlayer().getEntityId(), Constants.ID_GUI_BABYREQUEST), (EntityPlayerMP)targetInfo.getPlayer());
		}
	}

	private void onAcceptBaby(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		//MCA.packetHandler.sendPacketToPlayer(new PacketOnPlayerProcreate(senderInfo.getPlayer().getEntityId(), targetInfo.getPlayer().getEntityId()), (EntityPlayerMP)targetInfo.getPlayer());
		
		AbstractBaby itemBaby = null;
		boolean babyIsMale = Utility.getRandomGender();

		if (babyIsMale)
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyBoy;
		}

		else
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyGirl;
		}
		
		targetInfo.getPlayer().inventory.addItemStackToInventory(new ItemStack(itemBaby));
		
		senderInfo.getPropertiesList().babyIsMale = babyIsMale;
		targetInfo.getPropertiesList().babyIsMale = babyIsMale;
		senderInfo.getPropertiesList().babyExists = true;
		targetInfo.getPropertiesList().babyExists = true;
		
		senderInfo.getManager().saveWorldProperties();
		targetInfo.getManager().saveWorldProperties();
		
		MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(targetInfo.getPlayer().getEntityId(), Constants.ID_GUI_NAMECHILD), (EntityPlayerMP)targetInfo.getPlayer());
	}

	private void onDeclineBaby(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.havebaby.decline", senderInfo.getPlayer()), (EntityPlayerMP) targetInfo.getPlayer());		
	}

	private void onDivorce(PlayerInfo senderInfo, PlayerInfo targetInfo)
	{
		WorldPropertiesList senderList = senderInfo.getPropertiesList();
		WorldPropertiesList targetList = targetInfo.getPropertiesList();

		senderList.playerSpouseName = "";
		senderList.playerSpouseID = 0;

		targetList.playerSpouseName = "";
		targetList.playerSpouseID = 0;

		senderInfo.getManager().saveWorldProperties();
		targetInfo.getManager().saveWorldProperties();

		MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.divorce.successful", senderInfo.getPlayer()), (EntityPlayerMP) senderInfo.getPlayer());
		MCA.packetHandler.sendPacketToPlayer(new PacketNotifyLocalized("multiplayer.command.output.divorce.successful", senderInfo.getPlayer()), (EntityPlayerMP) targetInfo.getPlayer());
	}

	private boolean doesPlayerHaveWeddingRing(EntityPlayer player)
	{
		boolean result = false;

		for (int i = 0; i < player.inventory.mainInventory.length; i++)
		{
			final ItemStack stack = player.inventory.mainInventory[i];

			if (stack != null && stack.getItem() instanceof ItemWeddingRing)
			{
				result = true;
				break;
			}
		}

		return result;
	}
}

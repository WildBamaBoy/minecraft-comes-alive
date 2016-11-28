/*******************************************************************************
 * PacketCodec.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.enums.EnumPacketType;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.core.IEnforcedCore;
import com.radixshock.radixcore.network.AbstractPacketCodec;
import com.radixshock.radixcore.network.Packet;

/**
 * MCA's packet codec.
 */
public final class PacketCodec extends AbstractPacketCodec
{
	/**
	 * Constructor
	 * 
	 * @param	mod	The owner mod.
	 */
	public PacketCodec(IEnforcedCore mod) 
	{
		super(mod);
	}

	public void encode(Packet packet, ChannelHandlerContext context, ByteBuf buffer)
	{
		EnumPacketType type = (EnumPacketType)packet.packetType;

		try
		{
			switch (type)
			{
			case AddAI:
				buffer.writeInt((Integer) packet.arguments[0]);
				break;

			case AddBaby:
				buffer.writeBoolean((Boolean) packet.arguments[0]);
				break;

			case ArrangedMarriageParticles:
				buffer.writeInt((Integer) packet.arguments[0]);
				buffer.writeInt((Integer) packet.arguments[1]);
				break;

			case BabyInfo:
				writeObject(buffer, packet.arguments[0]);
				break;

			case BroadcastKillEntity:
				buffer.writeInt((Integer) packet.arguments[0]);
				break;

			case AddBabyRequest:
				writeObject(buffer, packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case AddMarriageRequest:
				writeObject(buffer, packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case RemoveBabyRequest:
				writeObject(buffer, packet.arguments[0]);
				break;

			case RemoveMarriageRequest:
				writeObject(buffer, packet.arguments[0]);
				break;

			case ClientSideCommand:
				writeObject(buffer, packet.arguments[0]);
				break;

			case GiveRelationshipGift:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;
			
			case GiveAid:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;
				
			case Engagement:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;

			case ForceRespawn:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				buffer.writeInt((Integer)packet.arguments[2]);
				buffer.writeInt((Integer)packet.arguments[3]);
				break;

			case GetTombstoneText:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				buffer.writeInt((Integer)packet.arguments[2]);
				break;

			case HaveBaby:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				break;

			case KillEntity:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;

			case MountHorse:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				break;

			case NameBaby:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeBoolean((Boolean)packet.arguments[1]);
				break;

			case NotifyPlayer:
				buffer.writeInt((Integer)packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case OpenGui:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeByte((Byte)packet.arguments[1]);
				break;
			case PlayerMarriage:
				buffer.writeInt((Integer)packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				buffer.writeInt((Integer)packet.arguments[2]);
				break;

			case RemoveItem:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				buffer.writeInt((Integer)packet.arguments[2]);
				buffer.writeInt((Integer)packet.arguments[3]);
				break;

			case ReturnInventory:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;

			case SayLocalized:
				boolean hasPlayer = packet.arguments[0] != null;
				boolean hasEntity = packet.arguments[1] != null;
				boolean hasPrefix = packet.arguments[4] != null;
				boolean hasSuffix = packet.arguments[5] != null;

				buffer.writeBoolean(hasPlayer);
				buffer.writeBoolean(hasEntity);
				buffer.writeBoolean(hasPrefix);
				buffer.writeBoolean(hasSuffix);

				if (hasPlayer)
				{
					writeObject(buffer, ((EntityPlayer)packet.arguments[0]).getCommandSenderName());
				}

				if (hasEntity)
				{
					buffer.writeInt(((AbstractEntity)packet.arguments[1]).getEntityId());
				}

				writeObject(buffer, packet.arguments[2]);
				buffer.writeBoolean((Boolean) packet.arguments[3]);

				if (hasPrefix)
				{
					writeObject(buffer, packet.arguments[4]);
				}

				if (hasSuffix)
				{
					writeObject(buffer, packet.arguments[5]);
				}

				break;

			case SetChore:
				buffer.writeInt((Integer)packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case SetFamilyTree:
				buffer.writeInt((Integer)packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case SetFieldValue:
				buffer.writeInt((Integer) packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				writeObject(buffer, packet.arguments[2]);
				break;

			case SetInventory:
				buffer.writeInt((Integer)packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case SetPosition:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeDouble((Double)packet.arguments[1]);
				buffer.writeDouble((Double)packet.arguments[2]);
				buffer.writeDouble((Double)packet.arguments[3]);
				break;

			case SetTarget:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				break;

			case SetTombstoneText:
				buffer.writeInt((Integer)packet.arguments[0]);
				buffer.writeInt((Integer)packet.arguments[1]);
				buffer.writeInt((Integer)packet.arguments[2]);
				writeObject(buffer, packet.arguments[3]);
				writeObject(buffer, packet.arguments[4]);
				writeObject(buffer, packet.arguments[5]);
				writeObject(buffer, packet.arguments[6]);
				break;

			case SetWorldProperties:
				writeObject(buffer, packet.arguments[0]);
				break;

			case StartTrade:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;

			case StopJumping:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;

			case SwingArm:
				buffer.writeInt((Integer)packet.arguments[0]);
				break;

			case SyncEditorSettings:
				buffer.writeInt((Integer)packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				buffer.writeBoolean((Boolean)packet.arguments[2]);
				buffer.writeInt((Integer)packet.arguments[3]);
				buffer.writeFloat((Float)packet.arguments[4]);
				buffer.writeFloat((Float)packet.arguments[5]);
				buffer.writeFloat((Float)packet.arguments[6]);
				buffer.writeInt((Integer)packet.arguments[7]);
				writeObject(buffer, packet.arguments[8]);
				writeObject(buffer, packet.arguments[9]);
				break;

			case SyncRequest:
				buffer.writeInt((Integer) packet.arguments[0]);
				break;

			case Sync:
				buffer.writeInt((Integer) packet.arguments[0]);
				writeObject(buffer, packet.arguments[1]);
				break;

			case UpdateFurnace:
				buffer.writeInt((Integer) packet.arguments[0]);
				buffer.writeBoolean((Boolean) packet.arguments[1]);
				break;

			default:
				break;
			}
		}

		catch (Throwable e)
		{
			MCA.getInstance().getLogger().log(e);
		}
	}

	public void decode(Packet packet, ChannelHandlerContext context, ByteBuf buffer)
	{
		EnumPacketType type = (EnumPacketType)packet.packetType;

		try
		{
			switch (type)
			{
			case AddAI:
				packet.arguments[0] = buffer.readInt();
				break;

			case AddBaby:
				packet.arguments[0] = buffer.readBoolean();
				break;

			case ArrangedMarriageParticles:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				break;

			case BabyInfo:
				packet.arguments[0] = readObject(buffer);
				break;

			case BroadcastKillEntity:
				packet.arguments[0] = buffer.readInt();
				break;

			case AddBabyRequest:
				packet.arguments[0] = readObject(buffer);
				packet.arguments[1] = readObject(buffer);
				break;

			case AddMarriageRequest:
				packet.arguments[0] = readObject(buffer);
				packet.arguments[1] = readObject(buffer);
				break;

			case RemoveBabyRequest:
				packet.arguments[0] = readObject(buffer);
				break;

			case RemoveMarriageRequest:
				packet.arguments[0] = readObject(buffer);
				break;

			case ClientSideCommand:
				packet.arguments[0] = readObject(buffer);
				break;
				
			case GiveRelationshipGift:
				packet.arguments[0] = buffer.readInt();
				break;
			
			case GiveAid:
				packet.arguments[0] = buffer.readInt();
				break;
				
			case Engagement:
				packet.arguments[0] = buffer.readInt();
				break;

			case ForceRespawn:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				packet.arguments[2] = buffer.readInt();
				packet.arguments[3] = buffer.readInt();
				break;

			case GetTombstoneText:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				packet.arguments[2] = buffer.readInt();
				break;

			case HaveBaby:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				break;

			case KillEntity:
				packet.arguments[0] = buffer.readInt();
				break;

			case MountHorse:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				break;

			case NameBaby:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readBoolean();
				break;

			case NotifyPlayer:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				break;

			case OpenGui:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readByte();
				break;

			case PlayerMarriage:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				packet.arguments[2] = buffer.readInt();
				break;

			case RemoveItem:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				packet.arguments[2] = buffer.readInt();
				packet.arguments[3] = buffer.readInt();
				break;

			case ReturnInventory:
				packet.arguments[0] = buffer.readInt();
				break;

			case SayLocalized:
				boolean hasPlayer = buffer.readBoolean();
				boolean hasEntity = buffer.readBoolean();
				boolean hasPrefix = buffer.readBoolean();
				boolean hasSuffix = buffer.readBoolean();

				final String playerName = hasPlayer ? (String) readObject(buffer) : null;
				final int entityId = hasEntity ? buffer.readInt() : -1;
				final String phraseId = (String) readObject(buffer);
				final boolean useCharacterType = buffer.readBoolean();
				final String prefix = hasPrefix ? (String) readObject(buffer) : null;
				final String suffix = hasSuffix ? (String) readObject(buffer) : null;

				packet.arguments = new Object[10];
				packet.arguments[0] = hasPlayer;
				packet.arguments[1] = hasEntity;
				packet.arguments[2] = hasPrefix;
				packet.arguments[3] = hasSuffix;
				packet.arguments[4] = playerName;
				packet.arguments[5] = entityId;
				packet.arguments[6] = phraseId;
				packet.arguments[7] = useCharacterType;
				packet.arguments[8] = prefix;
				packet.arguments[9] = suffix;
				break;

			case SetChore:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				break;

			case SetFamilyTree:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				break;

			case SetFieldValue:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				packet.arguments[2] = readObject(buffer);
				break;

			case SetInventory:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				break;

			case SetPosition:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1]= buffer.readDouble();
				packet.arguments[2] = buffer.readDouble();
				packet.arguments[3] = buffer.readDouble();
				break;

			case SetTarget:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				break;

			case SetTombstoneText:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readInt();
				packet.arguments[2] = buffer.readInt();
				packet.arguments[3] = readObject(buffer);
				packet.arguments[4] = readObject(buffer);
				packet.arguments[5] = readObject(buffer);
				packet.arguments[6] = readObject(buffer);
				break;

			case SetWorldProperties:
				packet.arguments[0] = readObject(buffer);
				break;

			case StartTrade:
				packet.arguments[0] = buffer.readInt();
				break;

			case StopJumping:
				packet.arguments[0] = buffer.readInt();
				break;

			case SwingArm:
				packet.arguments[0] = buffer.readInt();
				break;

			case SyncEditorSettings:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				packet.arguments[2] = buffer.readBoolean();
				packet.arguments[3] = buffer.readInt();
				packet.arguments[4] = buffer.readFloat();
				packet.arguments[5] = buffer.readFloat();
				packet.arguments[6] = buffer.readFloat();
				packet.arguments[7] = buffer.readInt();
				packet.arguments[8] = readObject(buffer);
				packet.arguments[9] = readObject(buffer);
				break;

			case SyncRequest:
				packet.arguments[0] = buffer.readInt();
				break;

			case Sync:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = readObject(buffer);
				break;

			case UpdateFurnace:
				packet.arguments[0] = buffer.readInt();
				packet.arguments[1] = buffer.readBoolean();
				break;

			default:
				break;
			}
		}
		
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}

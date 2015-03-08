package radixcore.packets;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;

import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;
import radixcore.data.AbstractPlayerData;
import radixcore.data.DataContainer;
import radixcore.network.ByteBufIO;
import radixcore.util.RadixExcept;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDataContainer extends AbstractPacket implements IMessage, IMessageHandler<PacketDataContainer, IMessage>
{
	private String ownerModId;
	private AbstractPlayerData playerData;
	
	public PacketDataContainer()
	{
	}

	public PacketDataContainer(String ownerModId, AbstractPlayerData playerData)
	{
		this.ownerModId = ownerModId;
		this.playerData = playerData;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.ownerModId = (String) ByteBufIO.readObject(byteBuf);
		this.playerData = (AbstractPlayerData) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, ownerModId);
		ByteBufIO.writeObject(byteBuf, playerData);
	}

	@Override
	public IMessage onMessage(PacketDataContainer packet, MessageContext context)
	{
		try
		{	
			for (ModMetadataEx mod : RadixCore.getRegisteredMods())
			{
				boolean found = false;
				
				if (mod.modId.equals(packet.ownerModId))
				{
					for (Field f : mod.classContainingClientDataContainer.getDeclaredFields())
					{
						if (f.getType().equals(DataContainer.class))
						{
							f.set(null, new DataContainer(packet.playerData));
							found = true;
							break;
						}
					}
					
					if (!found)
					{
						RadixExcept.logFatalCatch(new NullPointerException(), "Unable to find static reference to client data container class in " + mod.classContainingClientDataContainer.toString());
					}
				}
			}
		}
		
		catch (IllegalArgumentException e) 
		{
			RadixExcept.logFatalCatch(e, "IllegalArgumentException while creating client data container for " + packet.ownerModId);
		} 
		
		catch (IllegalAccessException e) 
		{
			RadixExcept.logFatalCatch(e, "IllegalAccessException while creating client data container for " + packet.ownerModId + ". Make sure client container field is public and static!");
		}
		
		return null;
	}
}

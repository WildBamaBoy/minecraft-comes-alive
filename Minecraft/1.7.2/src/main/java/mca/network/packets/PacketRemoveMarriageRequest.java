package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketRemoveMarriageRequest extends AbstractPacket implements IMessage, IMessageHandler<PacketRemoveMarriageRequest, IMessage>
{
	private String playerName;
	
	public PacketRemoveMarriageRequest()
	{
	}
	
	public PacketRemoveMarriageRequest(String playerName)
	{
		this.playerName = playerName;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		this.playerName = (String) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		ByteBufIO.writeObject(byteBuf, playerName);
	}

	@Override
	public IMessage onMessage(PacketRemoveMarriageRequest packet, MessageContext context) 
	{
		MCA.getInstance().marriageRequests.remove(packet.playerName);
		return null;
	}
}

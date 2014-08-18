package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketRemoveBabyRequest extends AbstractPacket implements IMessage, IMessageHandler<PacketRemoveBabyRequest, IMessage>
{
	private String playerName;
	
	public PacketRemoveBabyRequest()
	{
	}
	
	public PacketRemoveBabyRequest(String playerName)
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
	public IMessage onMessage(PacketRemoveBabyRequest packet, MessageContext context) 
	{
		MCA.getInstance().babyRequests.remove(packet.playerName);
		return null;
	}
}

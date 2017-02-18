package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.util.TutorialManager;
import mca.util.TutorialMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketSetTutorialMessage extends AbstractPacket<PacketSetTutorialMessage>
{
	private TutorialMessage tutorialMessage;

	public PacketSetTutorialMessage()
	{
	}

	public PacketSetTutorialMessage(TutorialMessage message)
	{
		this.tutorialMessage = message;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		tutorialMessage = (TutorialMessage) RadixNettyIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		RadixNettyIO.writeObject(byteBuf, tutorialMessage);
	}

	@Override
	public void processOnGameThread(PacketSetTutorialMessage packet, MessageContext context) 
	{
		TutorialManager.setTutorialMessage(packet.tutorialMessage);		
	}
}

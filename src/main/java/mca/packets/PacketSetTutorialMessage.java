package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.util.TutorialManager;
import mca.util.TutorialMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

public class PacketSetTutorialMessage extends AbstractPacket implements IMessage, IMessageHandler<PacketSetTutorialMessage, IMessage>
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
		tutorialMessage = (TutorialMessage) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, tutorialMessage);
	}

	@Override
	public IMessage onMessage(PacketSetTutorialMessage packet, MessageContext context)
	{
		TutorialManager.setTutorialMessage(packet.tutorialMessage);
		return null;
	}
}

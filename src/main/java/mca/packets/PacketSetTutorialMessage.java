package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.util.TutorialManager;
import mca.util.TutorialMessage;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketSetTutorialMessage packet = (PacketSetTutorialMessage)message;
		TutorialManager.setTutorialMessage(packet.tutorialMessage);		
	}
}

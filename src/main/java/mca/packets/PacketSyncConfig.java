package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Config;
import mca.core.MCA;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

public class PacketSyncConfig extends AbstractPacket implements IMessage, IMessageHandler<PacketSyncConfig, IMessage>
{
	private Config configObject;
	
	public PacketSyncConfig()
	{
	}

	public PacketSyncConfig(Config configObject)
	{
		this.configObject = configObject;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		configObject = (Config) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, configObject);
	}

	@Override
	public IMessage onMessage(PacketSyncConfig packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(packet, context);
		return null;
	}


	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketSyncConfig packet = (PacketSyncConfig) message;
		
		MCA.setConfig(packet.configObject);
		MCA.getLog().info("Received and applied server-side configuration.");
	}
}

package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.Config;
import mca.core.MCA;
import mca.util.TutorialManager;
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
		MCA.setConfig(packet.configObject);
		MCA.getLog().info("Received and applied server-side configuration.");
		return null;
	}
}

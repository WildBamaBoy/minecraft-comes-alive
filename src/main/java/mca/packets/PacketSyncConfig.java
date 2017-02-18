package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Config;
import mca.core.MCA;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketSyncConfig extends AbstractPacket<PacketSyncConfig>
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
		configObject = (Config) RadixNettyIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		RadixNettyIO.writeObject(byteBuf, configObject);
	}

	@Override
	public void processOnGameThread(PacketSyncConfig packet, MessageContext context) 
	{	
		MCA.setConfig(packet.configObject);
		MCA.getLog().info("Received and applied server-side configuration.");
	}
}

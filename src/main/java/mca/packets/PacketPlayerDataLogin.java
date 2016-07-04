package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

/* Player data change received from a player. */
public class PacketPlayerDataLogin extends AbstractPacket implements IMessage, IMessageHandler<PacketPlayerDataLogin, IMessage>
{
	private NBTPlayerData playerData;
	
	public PacketPlayerDataLogin()
	{
	}

	public PacketPlayerDataLogin(NBTPlayerData playerData)
	{
		this.playerData = playerData;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		playerData = (NBTPlayerData) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, playerData);
	}

	@Override
	public IMessage onMessage(PacketPlayerDataLogin packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketPlayerDataLogin packet = (PacketPlayerDataLogin)message;
		MCA.myPlayerData = packet.playerData;
	}
}

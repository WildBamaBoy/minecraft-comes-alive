package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
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
		MCA.myPlayerData = packet.playerData;
		System.out.println("DATA LOGIN");
		return null;
	}
}

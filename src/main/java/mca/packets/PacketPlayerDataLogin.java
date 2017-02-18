package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketPlayerDataLogin extends AbstractPacket<PacketPlayerDataLogin>
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
		playerData = (NBTPlayerData) RadixNettyIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		RadixNettyIO.writeObject(byteBuf, playerData);
	}

	@Override
	public void processOnGameThread(PacketPlayerDataLogin packet, MessageContext context) 
	{
		MCA.myPlayerData = packet.playerData;
	}
}

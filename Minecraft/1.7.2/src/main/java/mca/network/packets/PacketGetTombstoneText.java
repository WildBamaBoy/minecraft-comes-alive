package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketGetTombstoneText extends AbstractPacket implements IMessage, IMessageHandler<PacketGetTombstoneText, IMessage>
{
	private int posX;
	private int posY;
	private int posZ;

	public PacketGetTombstoneText()
	{
	}

	public PacketGetTombstoneText(int posX, int posY, int posZ)
	{
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		this.posX = byteBuf.readInt();
		this.posY = byteBuf.readInt();
		this.posZ = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(posX);
		byteBuf.writeInt(posY);
		byteBuf.writeInt(posZ);
	}

	@Override
	public IMessage onMessage(PacketGetTombstoneText packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final TileEntityTombstone tombstone = (TileEntityTombstone)player.worldObj.getTileEntity(packet.posX, packet.posY, packet.posZ);

		if (tombstone != null)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketSetTombstoneText(packet.posX, packet.posY, packet.posZ, 
					tombstone.signText[0], tombstone.signText[1], tombstone.signText[2], tombstone.signText[3]), (EntityPlayerMP)player);
		}

		return null;
	}
}

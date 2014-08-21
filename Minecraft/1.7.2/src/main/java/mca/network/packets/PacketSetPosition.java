package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSetPosition extends AbstractPacket implements IMessage, IMessageHandler<PacketSetPosition, IMessage>
{
	private int entityId;
	private double posX;
	private double posY;
	private double posZ;
	
	public PacketSetPosition()
	{
	}
	
	public PacketSetPosition(int entityId, double posX, double posY, double posZ)
	{
		this.entityId = entityId;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		posX = byteBuf.readDouble();
		posY = byteBuf.readDouble();
		posZ = byteBuf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeDouble(posX);
		byteBuf.writeDouble(posY);
		byteBuf.writeDouble(posZ);
	}

	@Override
	public IMessage onMessage(PacketSetPosition packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.entityId);
		
		if (entity != null)
		{
			entity.setPosition(packet.posX, packet.posY, packet.posZ);
		}
		
		return null;
	}
}

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketStopJumping extends AbstractPacket implements IMessage, IMessageHandler<PacketStopJumping, IMessage>
{
	private int entityId;
	
	public PacketStopJumping()
	{
	}
	
	public PacketStopJumping(int entityId)
	{
		this.entityId = entityId;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
	}

	@Override
	public IMessage onMessage(PacketStopJumping packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.entityId);

		entity.setJumping(false);
		return null;
	}
}

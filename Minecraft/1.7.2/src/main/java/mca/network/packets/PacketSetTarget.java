package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.entity.AbstractEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSetTarget extends AbstractPacket implements IMessage, IMessageHandler<PacketSetTarget, IMessage>
{
	private int entityId;
	private int targetId;
	
	public PacketSetTarget()
	{
	}
	
	public PacketSetTarget(int entityId, int targetId)
	{
		this.entityId = entityId;
		this.targetId = targetId;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		targetId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeInt(targetId);
	}

	@Override
	public IMessage onMessage(PacketSetTarget packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		
		for (final Object obj : player.worldObj.loadedEntityList)
		{
			Entity entity = (Entity)obj;

			if (entity.getEntityId() == packet.entityId)
			{
				final AbstractEntity clientEntity = (AbstractEntity)entity;

				if (packet.targetId == 0)
				{
					clientEntity.target = null;
				}

				else
				{
					clientEntity.target = (EntityLivingBase)clientEntity.worldObj.getEntityByID(packet.targetId);
				}
			}
		}
		
		return null;
	}
}

package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSwingArm extends AbstractPacket implements IMessage, IMessageHandler<PacketSwingArm, IMessage>
{
	private int entityId;
	
	public PacketSwingArm()
	{
	}
	
	public PacketSwingArm(int entityId)
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
	public IMessage onMessage(PacketSwingArm packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.entityId);

		if (entity != null)
		{
			entity.swingItem();
		}
		
		return null;
	}
}

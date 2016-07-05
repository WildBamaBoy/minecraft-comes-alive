package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

public class PacketSetSize extends AbstractPacket implements IMessage, IMessageHandler<PacketSetSize, IMessage>
{
	private String entityUUID;
	private int entityId;
	private float width;
	private float height;

	public PacketSetSize()
	{
	}

	public PacketSetSize(EntityHuman human, float width, float height)
	{
		this.entityUUID = human.getUniqueID().toString();
		this.entityId = human.getEntityId();
		this.width = width;
		this.height = height;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.entityUUID = (String) ByteBufIO.readObject(byteBuf);
		this.entityId = byteBuf.readInt();
		this.width = byteBuf.readFloat();
		this.height = byteBuf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, this.entityUUID);
		byteBuf.writeInt(entityId);
		byteBuf.writeFloat(this.width);
		byteBuf.writeFloat(this.height);
	}

	@Override
	public IMessage onMessage(PacketSetSize packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketSetSize packet = (PacketSetSize)message;
		EntityPlayer player = getPlayer(context);
		World world = player.worldObj;
		EntityHuman human = null;

		for (Object obj : world.loadedEntityList)
		{
			Entity entity = (Entity)obj;
			
			try
			{
				//Two-factor check for different MC versions. UUIDs do not appear to work properly in 1.7.10, and
				//I believe actual entity IDs are deprecated later.
				if (entity.getUniqueID().toString().equals(packet.entityUUID) || entity.getEntityId() == packet.entityId)
				{
					human = (EntityHuman) entity;
					break;
				}
			}

			catch (Exception e) //ClassCast or NullPointer is possible here. Ignore.
			{
				continue;
			}
		}

		if (human != null)
		{
			human.setSizeOverride(packet.width, packet.height);
		}
	}
}

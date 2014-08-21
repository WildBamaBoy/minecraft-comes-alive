package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncRequest extends AbstractPacket implements IMessage, IMessageHandler<PacketSyncRequest, IMessage>
{
	private int entityId;

	public PacketSyncRequest()
	{
	}

	public PacketSyncRequest(int entityId)
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
	public IMessage onMessage(PacketSyncRequest packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);

		if (player != null)
		{
			for (final World world : MinecraftServer.getServer().worldServers)
			{
				for (final Object obj : world.loadedEntityList)
				{
					if (obj instanceof AbstractEntity)
					{
						AbstractEntity entity = (AbstractEntity)obj;

						if (entity != null && entity.getEntityId() == packet.entityId)
						{
							MCA.packetHandler.sendPacketToPlayer(new PacketSync(entity.getEntityId(), entity), (EntityPlayerMP) player);
							MCA.packetHandler.sendPacketToPlayer(new PacketSetInventory(entity.getEntityId(), entity.inventory), (EntityPlayerMP) player); 
							MCA.getInstance().entitiesMap.put(entity.mcaID, entity);
							break;
						}
					}
				}
			}
		}
		
		return null;
	}
}

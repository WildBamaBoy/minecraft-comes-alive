package mca.packets;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.net.AbstractPacket;

public class PacketCallVillager extends AbstractPacket<PacketCallVillager>
{
	private UUID callUUID;
	private boolean callAllRelated;
	
	public PacketCallVillager()
	{
	}

	public PacketCallVillager(boolean callAllRelated)
	{
		this.callUUID = new UUID(0L, 0L);
		this.callAllRelated = true;		
	}

	public PacketCallVillager(UUID callUUID)
	{
		this.callUUID = callUUID;
		this.callAllRelated = false;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.callUUID = new UUID(byteBuf.readLong(), byteBuf.readLong());
		this.callAllRelated = byteBuf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeLong(this.callUUID.getMostSignificantBits());
		byteBuf.writeLong(this.callUUID.getLeastSignificantBits());
		byteBuf.writeBoolean(callAllRelated);
	}

	@Override
	public void processOnGameThread(PacketCallVillager packet, MessageContext context) 
	{
		EntityPlayer sender = this.getPlayer(context);
		
		if (packet.callAllRelated)
		{
			for (final Object obj : sender.world.loadedEntityList)
			{
				if (obj instanceof EntityVillagerMCA)
				{
					EntityVillagerMCA human = (EntityVillagerMCA)obj;
					
					if (human.attributes.isPlayerAParent(sender) || human.attributes.getPlayerSpouseInstance() == sender)
					{
						human.setPositionAndUpdate(sender.posX, sender.posY, sender.posZ);
					}
				}
			}
		}
		
		else
		{
			EntityVillagerMCA human = null;
			
			for (Object obj : sender.world.loadedEntityList)
			{
				if (obj instanceof EntityVillagerMCA)
				{
					EntityVillagerMCA theHuman = (EntityVillagerMCA)obj;
					
					if (theHuman.getUniqueID().equals(packet.callUUID))
					{
						human = theHuman;
						break;
					}
				}
			}
			
			if (human != null)
			{
				human.setPositionAndUpdate(sender.posX, sender.posY, sender.posZ);
			}
		}
	}
}

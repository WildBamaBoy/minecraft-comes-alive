package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketSyncPlayerMemory extends AbstractPacket<PacketSyncPlayerMemory>
{
	private int entityId;
	private PlayerMemory memory;

	public PacketSyncPlayerMemory()
	{
	}

	public PacketSyncPlayerMemory(int entityId, PlayerMemory memory)
	{
		this.entityId = entityId;
		this.memory = memory;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.entityId = byteBuf.readInt();
		this.memory = (PlayerMemory) RadixNettyIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{		
		byteBuf.writeInt(entityId);
		RadixNettyIO.writeObject(byteBuf, this.memory);
	}

	@Override
	public void processOnGameThread(PacketSyncPlayerMemory packet, MessageContext context) 
	{
		EntityPlayer player = getPlayer(context);
		EntityVillagerMCA human = (EntityVillagerMCA) player.worldObj.getEntityByID(packet.entityId);
		
		if (human != null && packet.memory != null) //Noticing NPE here with varying causes. Can be ignored.
		{
			human.setPlayerMemory(player, packet.memory);
		}
	}
}

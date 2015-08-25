package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import radixcore.util.RadixExcept;

public class PacketSyncPlayerMemory extends AbstractPacket implements IMessage, IMessageHandler<PacketSyncPlayerMemory, IMessage>
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
		this.memory = (PlayerMemory) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{		
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, this.memory);
	}

	@Override
	public IMessage onMessage(PacketSyncPlayerMemory packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		try
		{
			PacketSyncPlayerMemory packet = (PacketSyncPlayerMemory) message;
			EntityPlayer player = getPlayer(context);
			EntityHuman human = (EntityHuman) player.worldObj.getEntityByID(packet.entityId);
			
			if (human != null && packet.memory != null) //Noticing NPE here with varying causes. Can be ignored.
			{
				human.setPlayerMemory(player, packet.memory);
			}
		}
		
		catch (Exception e)
		{
			RadixExcept.logErrorCatch(e, "Unexpected error while syncing player memory.");
		}
	}
}

package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.helpers.ExceptHelper;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

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
		try
		{
			EntityPlayer player = getPlayer(context);
			EntityHuman human = (EntityHuman) player.worldObj.getEntityByID(packet.entityId);
			human.setPlayerMemory(player, packet.memory);
		}
		
		catch (Exception e)
		{
			ExceptHelper.logErrorCatch(e, "Unexpected error while syncing player memory.");
		}
		
		return null;
	}
}

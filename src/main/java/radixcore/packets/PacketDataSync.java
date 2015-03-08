package radixcore.packets;

import io.netty.buffer.ByteBuf;

import java.util.Map;

import radixcore.data.DataWatcherEx;
import radixcore.data.IWatchable;
import radixcore.data.WatchedObjectEx;
import radixcore.network.ByteBufIO;
import radixcore.util.RadixExcept;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDataSync extends AbstractPacket implements IMessage, IMessageHandler<PacketDataSync, IMessage>
{
	private int entityId;
	private Map dataWatcherData;
	
	public PacketDataSync()
	{
	}

	public PacketDataSync(int entityId, DataWatcherEx dataWatcherEx)
	{
		this.entityId = entityId;
		this.dataWatcherData = dataWatcherEx.getWatchedDataMap();
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.entityId = byteBuf.readInt();
		this.dataWatcherData = (Map) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(this.entityId);
		ByteBufIO.writeObject(byteBuf, this.dataWatcherData);		
	}

	@Override
	public IMessage onMessage(PacketDataSync packet, MessageContext context)
	{
		IWatchable entity = (IWatchable)this.getPlayerClient().worldObj.getEntityByID(packet.entityId);
		
		try
		{
			DataWatcherEx dataWatcherEx = entity.getDataWatcherEx();
			
			for (Object obj : packet.dataWatcherData.values())
			{
				WatchedObjectEx recvObject = (WatchedObjectEx)obj;
				WatchedObjectEx currentObject = dataWatcherEx.getWatchedObject(recvObject.getDataValueId());
				
				currentObject.setObject(recvObject.getObject());
			}
		}
		
		catch (Throwable e)
		{
			RadixExcept.logErrorCatch(e, "Unexpected error while processing received sync data.");
		}
		
		return null;
	}
}

package radixcore.packets;

import io.netty.buffer.ByteBuf;
import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;
import radixcore.data.AbstractPlayerData;
import radixcore.data.DataContainer;
import radixcore.data.DataWatcherEx;
import radixcore.data.IWatchable;
import radixcore.network.ByteBufIO;
import radixcore.util.RadixExcept;
import radixcore.util.RadixReflect;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketWatchedUpdateC extends AbstractPacket implements IMessage, IMessageHandler<PacketWatchedUpdateC, IMessage>
{
	private int entityId;
	private String modId;
	private int watchedId;
	private Object watchedValue;

	public PacketWatchedUpdateC()
	{
	}

	public PacketWatchedUpdateC(int entityId, int watchedId, Object watchedValue)
	{
		this.entityId = entityId;
		this.watchedId = watchedId;
		this.watchedValue = watchedValue;
	}

	public PacketWatchedUpdateC(String modId, int watchedId, Object watchedValue)
	{
		this.modId = modId;
		this.watchedId = watchedId;
		this.watchedValue = watchedValue;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.entityId = byteBuf.readInt();
		this.modId = (String) ByteBufIO.readObject(byteBuf);
		this.watchedId = byteBuf.readInt();
		this.watchedValue = ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(this.entityId);
		ByteBufIO.writeObject(byteBuf, this.modId);
		byteBuf.writeInt(this.watchedId);
		ByteBufIO.writeObject(byteBuf, this.watchedValue);
	}
	
	@Override
	public IMessage onMessage(PacketWatchedUpdateC packet, MessageContext context)
	{
		try
		{
			IWatchable watchable = null;

			if (packet.modId != null)
			{
				ModMetadataEx modData = null;

				for (ModMetadataEx data : RadixCore.getRegisteredMods())
				{
					if (data.modId.equals(packet.modId))
					{
						modData = data;
					}
				}

				if (modData != null)
				{
					DataContainer container = RadixReflect.getStaticObjectOfTypeFromClass(DataContainer.class, modData.classContainingClientDataContainer);
					watchable = container != null ? container.getPlayerData(AbstractPlayerData.class) : null;
				}
			}

			else
			{
				watchable = (IWatchable)this.getPlayer(context).worldObj.getEntityByID(packet.entityId);
			}

			if (watchable != null)
			{
				DataWatcherEx dataWatcherEx = watchable.getDataWatcherEx();
				dataWatcherEx.updateObject(packet.watchedId, packet.watchedValue, false); //Do not dispatch client-side.
			}
		}

		catch (Throwable e)
		{
			RadixExcept.logErrorCatch(e, "Non-fatal error caught while updating watched object server-side.");
		}

		return null;
	}
}

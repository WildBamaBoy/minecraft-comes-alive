package radixcore.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import radixcore.ModMetadataEx;
import radixcore.RadixCore;
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

public class PacketWatchedUpdateS extends AbstractPacket implements IMessage, IMessageHandler<PacketWatchedUpdateS, IMessage>
{
	private int entityId;
	private String modId;
	private int watchedId;
	private Object watchedValue;

	public PacketWatchedUpdateS()
	{
	}

	public PacketWatchedUpdateS(int entityId, int watchedId, Object watchedValue)
	{
		this.entityId = entityId;
		this.watchedId = watchedId;
		this.watchedValue = watchedValue;
	}

	public PacketWatchedUpdateS(String modId, int watchedId, Object watchedValue)
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
	public IMessage onMessage(PacketWatchedUpdateS packet, MessageContext context)
	{
		try
		{
			EntityPlayer player = this.getPlayer(context);
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
					if (!MinecraftServer.getServer().isDedicatedServer())
					{
						DataContainer container = RadixReflect.getStaticObjectOfTypeFromClass(DataContainer.class, modData.classContainingClientDataContainer);
						watchable = container.getPlayerData(AbstractPlayerData.class);
					}

					else
					{
						watchable = MCA.getPlayerData(player);
					}
				}
			}

			else
			{
				watchable = (IWatchable)player.worldObj.getEntityByID(packet.entityId);
			}

			if (watchable != null)
			{
				DataWatcherEx dataWatcherEx = watchable.getDataWatcherEx();
				dataWatcherEx.updateObject(packet.watchedId, packet.watchedValue, true); //Server-side received info from client. Dispatch to all other clients.
			}
		}

		catch (Throwable e)
		{
			RadixExcept.logErrorCatch(e, "Non-fatal error caught while updating watched object server-side.");
		}

		return null;
	}
}

package mca.network.packets;

import io.netty.buffer.ByteBuf;

import java.util.Map;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketNotifyPlayer extends AbstractPacket implements IMessage, IMessageHandler<PacketNotifyPlayer, IMessage>
{
	private int entityId;
	private String phraseId;
	
	public PacketNotifyPlayer()
	{
	}
	
	public PacketNotifyPlayer(int entityId, String phraseId)
	{
		this.entityId = entityId;
		this.phraseId = phraseId;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		phraseId = (String) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, phraseId);
	}

	@Override
	public IMessage onMessage(PacketNotifyPlayer packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		AbstractEntity entity = null;

		try
		{
			entity = (AbstractEntity) player.worldObj.getEntityByID(packet.entityId);

			if (entity == null)
			{
				int mcaId = -1;

				for (Map.Entry<Integer, Integer> entry : MCA.getInstance().idsMap.entrySet())
				{
					if (entry.getValue() == packet.entityId)
					{
						mcaId = entry.getKey();
						break;
					}
				}

				if (mcaId > -1)
				{
					entity = MCA.getInstance().entitiesMap.get(mcaId);
				}
			}
		}

		catch (ClassCastException e) 
		{ 
			//Occurs when player passed as an argument.
		}

		player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(packet.phraseId, null, entity, false)));
		
		return null;
	}
}

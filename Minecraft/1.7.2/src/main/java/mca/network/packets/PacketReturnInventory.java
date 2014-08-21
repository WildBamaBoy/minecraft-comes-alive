package mca.network.packets;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnInventory extends AbstractPacket implements IMessage, IMessageHandler<PacketReturnInventory, IMessage>
{
	private int entityId;

	public PacketReturnInventory()
	{
	}

	public PacketReturnInventory(int entityId)
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
	public IMessage onMessage(PacketReturnInventory packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(packet.entityId);

		if (entity != null)
		{
			ArrayList<EntityItem> itemList = MCA.getInstance().deadPlayerInventories.get(player.getCommandSenderName());

			for (EntityItem item : itemList)
			{
				entity.entityDropItem(item.getEntityItem(), 0.3F);
			}

			MCA.getInstance().deadPlayerInventories.remove(player.getCommandSenderName());
		}
		
		return null;
	}
}

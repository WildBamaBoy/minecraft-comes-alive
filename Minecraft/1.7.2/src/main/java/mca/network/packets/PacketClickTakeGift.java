package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.util.LogicExtension;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketClickTakeGift extends AbstractPacket implements IMessage, IMessageHandler<PacketClickTakeGift, IMessage>
{
	private int interactingEntityId;

	public PacketClickTakeGift()
	{
	}

	public PacketClickTakeGift(int interactingEntityId)
	{
		this.interactingEntityId = interactingEntityId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		interactingEntityId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(interactingEntityId);
	}

	@Override
	public IMessage onMessage(PacketClickTakeGift packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.interactingEntityId);

		if (entity != null)
		{
			final ItemStack dropStack = LogicExtension.getGiftStackFromRelationship(player, entity);
			entity.entityDropItem(dropStack, 0.2F);
		}

		return null;
	}
}

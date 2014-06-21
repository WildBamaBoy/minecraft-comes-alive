package mca.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketRemoveItem extends AbstractPacket implements IMessage, IMessageHandler<PacketRemoveItem, IMessage>
{
	private int entityId;
	private int slot;
	private int amount;
	private int damage;
	
	public PacketRemoveItem()
	{
	}
	
	public PacketRemoveItem(int entityId, int slot, int amount, int damage)
	{
		this.entityId = entityId;
		this.slot = slot;
		this.amount = amount;
		this.damage = damage;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		slot = byteBuf.readInt();
		amount = byteBuf.readInt();
		damage = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeInt(slot);
		byteBuf.writeInt(amount);
		byteBuf.writeInt(damage);
	}

	@Override
	public IMessage onMessage(PacketRemoveItem packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		player.inventory.decrStackSize(slot, amount);
		
		return null;
	}
}

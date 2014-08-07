package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.inventory.Inventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSetInventory extends AbstractPacket implements IMessage, IMessageHandler<PacketSetInventory, IMessage>
{
	private int entityId;
	private Inventory inventory;

	public PacketSetInventory()
	{
	}

	public PacketSetInventory(int entityId, Inventory inventory)
	{
		this.entityId = entityId;
		this.inventory = inventory;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		inventory = (Inventory) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, inventory);
	}

	@Override
	public IMessage onMessage(PacketSetInventory packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(packet.entityId);

		if (entity != null)
		{
			packet.inventory.owner = entity;
			entity.inventory = packet.inventory;
			entity.inventory.setWornArmorItems();
		}

		if (!player.worldObj.isRemote)
		{
			MCA.packetHandler.sendPacketToAllPlayersExcept(new PacketSetInventory(entityId, inventory), (EntityPlayerMP)player);
		}

		return null;
	}
}

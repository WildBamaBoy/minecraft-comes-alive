package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.items.ItemBaby;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketBabyName extends AbstractPacket implements IMessage, IMessageHandler<PacketBabyName, IMessage>
{
	private String babyName;
	private int slot;
	
	public PacketBabyName()
	{
	}

	public PacketBabyName(String babyName, int slot)
	{
		this.babyName = babyName;
		this.slot = slot;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		babyName = (String) ByteBufIO.readObject(byteBuf);
		slot = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, babyName);
		byteBuf.writeInt(slot);
	}

	@Override
	public IMessage onMessage(PacketBabyName packet, MessageContext context)
	{
		EntityPlayer senderPlayer = this.getPlayer(context);
		ItemStack stack = senderPlayer.inventory.getStackInSlot(packet.slot);
		
		if (stack != null && stack.getItem() instanceof ItemBaby)
		{
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.setString("name", packet.babyName);
		}
		
		return null;
	}
}

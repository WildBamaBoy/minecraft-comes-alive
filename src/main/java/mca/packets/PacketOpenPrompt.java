package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiPrompt;
import mca.enums.EnumInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.packets.AbstractPacket;

public class PacketOpenPrompt extends AbstractPacket implements IMessage, IMessageHandler<PacketOpenPrompt, IMessage>
{
	private int senderId;
	private int targetId;
	private int interactionId;
	
	public PacketOpenPrompt()
	{
		//Required
	}

	public PacketOpenPrompt(EntityPlayer sender, EntityPlayer target, EnumInteraction interaction)
	{
		this.senderId = sender.getEntityId();
		this.targetId = target.getEntityId();
		this.interactionId = interaction.getId();
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		senderId = byteBuf.readInt();
		targetId = byteBuf.readInt();
		interactionId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(senderId);
		byteBuf.writeInt(targetId);
		byteBuf.writeInt(interactionId);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketOpenPrompt packet, MessageContext context)
	{
		EntityPlayer target = this.getPlayer(context);
		EntityPlayer sender = (EntityPlayer) target.worldObj.getEntityByID(packet.senderId);
		EnumInteraction interaction = EnumInteraction.fromId(packet.interactionId);
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiPrompt(sender, target, interaction));
		return null;
	}
}

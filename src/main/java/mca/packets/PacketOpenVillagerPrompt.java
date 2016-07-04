package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiVillagerPrompt;
import mca.entity.EntityHuman;
import mca.enums.EnumInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.packets.AbstractPacket;

public class PacketOpenVillagerPrompt extends AbstractPacket implements IMessage, IMessageHandler<PacketOpenVillagerPrompt, IMessage>
{
	private int senderId;
	private int targetId;
	private int interactionId;
	
	public PacketOpenVillagerPrompt()
	{
		//Required
	}

	public PacketOpenVillagerPrompt(EntityPlayer player, EntityHuman human, EnumInteraction interaction)
	{
		this.senderId = player.getEntityId();
		this.targetId = human.getEntityId();
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
	public IMessage onMessage(PacketOpenVillagerPrompt packet, MessageContext context)
	{
		EntityPlayer player = this.getPlayer(context);
		EntityHuman human = (EntityHuman) player.worldObj.getEntityByID(packet.targetId);
		EnumInteraction interaction = EnumInteraction.fromId(packet.interactionId);
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiVillagerPrompt(player, human, interaction));
		return null;
	}
}

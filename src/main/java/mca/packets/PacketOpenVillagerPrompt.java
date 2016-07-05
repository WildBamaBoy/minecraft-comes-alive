package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiVillagerPrompt;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.enums.EnumInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketOpenVillagerPrompt packet = (PacketOpenVillagerPrompt)message;
		
		EntityPlayer player = this.getPlayer(context);
		EntityHuman human = (EntityHuman) player.worldObj.getEntityByID(packet.targetId);
		EnumInteraction interaction = EnumInteraction.fromId(packet.interactionId);
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiVillagerPrompt(player, human, interaction));
	}
}

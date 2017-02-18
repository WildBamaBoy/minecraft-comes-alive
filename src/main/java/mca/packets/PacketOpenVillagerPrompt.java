package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiVillagerPrompt;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.modules.net.AbstractPacket;

public class PacketOpenVillagerPrompt extends AbstractPacket<PacketOpenVillagerPrompt>
{
	private int senderId;
	private int targetId;
	private int interactionId;
	
	public PacketOpenVillagerPrompt()
	{
		//Required
	}

	public PacketOpenVillagerPrompt(EntityPlayer player, EntityVillagerMCA human, EnumInteraction interaction)
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
	public void processOnGameThread(PacketOpenVillagerPrompt packet, MessageContext context) 
	{
		EntityPlayer player = this.getPlayer(context);
		EntityVillagerMCA human = (EntityVillagerMCA) player.worldObj.getEntityByID(packet.targetId);
		EnumInteraction interaction = EnumInteraction.fromId(packet.interactionId);
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiVillagerPrompt(player, human, interaction));
	}
}

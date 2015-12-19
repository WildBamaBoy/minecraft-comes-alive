package mca.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiNameBaby;
import mca.client.gui.GuiNameBaby;
import mca.core.MCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.packets.AbstractPacket;

public class PacketOpenBabyNameGUI extends AbstractPacket implements IMessage, IMessageHandler<PacketOpenBabyNameGUI, IMessage>
{
	private boolean isMale;
	
	public PacketOpenBabyNameGUI()
	{
	}

	public PacketOpenBabyNameGUI(boolean isMale)
	{
		this.isMale = isMale;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		isMale = byteBuf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeBoolean(isMale);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketOpenBabyNameGUI packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketOpenBabyNameGUI packet = (PacketOpenBabyNameGUI)message;
		EntityPlayer senderPlayer = this.getPlayer(context);
		Minecraft.getMinecraft().displayGuiScreen(new GuiNameBaby(senderPlayer, packet.isMale));		
	}
}

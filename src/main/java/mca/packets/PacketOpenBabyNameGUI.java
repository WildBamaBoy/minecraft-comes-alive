package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiNameBaby;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.packets.AbstractPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
		EntityPlayer senderPlayer = this.getPlayer(context);
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiNameBaby(senderPlayer, packet.isMale));
		
		return null;
	}
}

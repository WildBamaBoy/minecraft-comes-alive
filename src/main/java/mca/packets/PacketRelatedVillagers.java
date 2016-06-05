package mca.packets;

import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiWhistle;
import mca.data.VillagerSaveData;
import net.minecraft.client.Minecraft;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

public class PacketRelatedVillagers extends AbstractPacket implements IMessage, IMessageHandler<PacketRelatedVillagers, IMessage>
{	
	public List<VillagerSaveData> dataList;
	
	public PacketRelatedVillagers()
	{
		//Required
	}

	public PacketRelatedVillagers(List<VillagerSaveData> dataList)
	{
		this.dataList = dataList;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.dataList = (List<VillagerSaveData>) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, dataList);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketRelatedVillagers packet, MessageContext context)
	{
		if (Minecraft.getMinecraft().currentScreen instanceof GuiWhistle)
		{
			GuiWhistle gui = (GuiWhistle)Minecraft.getMinecraft().currentScreen;
			gui.setVillagerDataList(packet.dataList);
		}
		
		return null;
	}
}

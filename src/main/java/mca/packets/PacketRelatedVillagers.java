package mca.packets;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiWhistle;
import mca.entity.VillagerSaveData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketRelatedVillagers extends AbstractPacket<PacketRelatedVillagers>
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
		this.dataList = (List<VillagerSaveData>) RadixNettyIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		RadixNettyIO.writeObject(byteBuf, dataList);
	}

	@Override
	public void processOnGameThread(PacketRelatedVillagers packet, MessageContext context) 
	{
		if (Minecraft.getMinecraft().currentScreen instanceof GuiWhistle)
		{
			GuiWhistle gui = (GuiWhistle)Minecraft.getMinecraft().currentScreen;
			gui.setVillagerDataList(packet.dataList);
		}
	}
}

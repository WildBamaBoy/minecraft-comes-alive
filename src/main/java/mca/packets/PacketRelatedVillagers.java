package mca.packets;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiWhistle;
import mca.core.MCA;
import mca.data.VillagerSaveData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketRelatedVillagers packet = (PacketRelatedVillagers)message;
		
		if (Minecraft.getMinecraft().currentScreen instanceof GuiWhistle)
		{
			GuiWhistle gui = (GuiWhistle)Minecraft.getMinecraft().currentScreen;
			gui.setVillagerDataList(packet.dataList);
		}
	}
}

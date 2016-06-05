package mca.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.VillagerSaveData;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.packets.AbstractPacket;

public class PacketRequestRelatedVillagers extends AbstractPacket implements IMessage, IMessageHandler<PacketRequestRelatedVillagers, IMessage>
{	
	public PacketRequestRelatedVillagers()
	{
		//Required
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketRequestRelatedVillagers packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		EntityPlayer sender = this.getPlayer(context);
		List<VillagerSaveData> dataList = new ArrayList<VillagerSaveData>();
		
		for (final Object obj : sender.worldObj.loadedEntityList)
		{
			if (obj instanceof EntityHuman)
			{
				EntityHuman human = (EntityHuman)obj;
				
				if (human.isPlayerAParent(sender) || human.getPlayerSpouse() == sender)
				{
					dataList.add(VillagerSaveData.fromVillager(human, sender, null));
				}
			}
		}
		
		MCA.getPacketHandler().sendPacketToPlayer(new PacketRelatedVillagers(dataList), (EntityPlayerMP) sender);
	}
}

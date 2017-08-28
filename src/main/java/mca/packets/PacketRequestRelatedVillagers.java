package mca.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.TransitiveVillagerData;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.net.AbstractPacket;

public class PacketRequestRelatedVillagers extends AbstractPacket<PacketRequestRelatedVillagers>
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

	@Override
	public void processOnGameThread(PacketRequestRelatedVillagers packet, MessageContext context) 
	{
		EntityPlayer sender = this.getPlayer(context);
		List<TransitiveVillagerData> dataList = new ArrayList<TransitiveVillagerData>();
		
		for (final Object obj : sender.world.loadedEntityList)
		{
			if (obj instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA human = (EntityVillagerMCA)obj;
				
				if (human.attributes.isPlayerAParent(sender) || human.attributes.getPlayerSpouseInstance() == sender)
				{
					dataList.add(new TransitiveVillagerData(human.attributes));
				}
			}
		}
		
		MCA.getPacketHandler().sendPacketToPlayer(new PacketRelatedVillagers(dataList), (EntityPlayerMP) sender);
	}
}

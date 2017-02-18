package mca.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.VillagerSaveData;
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
		List<VillagerSaveData> dataList = new ArrayList<VillagerSaveData>();
		
		for (final Object obj : sender.worldObj.loadedEntityList)
		{
			if (obj instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA human = (EntityVillagerMCA)obj;
				
				if (human.isPlayerAParent(sender) || human.getPlayerSpouseInstance() == sender)
				{
					dataList.add(VillagerSaveData.fromVillager(human, sender, null));
				}
			}
		}
		
		MCA.getPacketHandler().sendPacketToPlayer(new PacketRelatedVillagers(dataList), (EntityPlayerMP) sender);
	}
}

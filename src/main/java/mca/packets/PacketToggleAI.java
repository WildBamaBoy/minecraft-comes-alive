package mca.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.ai.AICooking;
import mca.ai.AIFarming;
import mca.ai.AIFishing;
import mca.ai.AIHunting;
import mca.ai.AIMining;
import mca.ai.AIWoodcutting;
import mca.core.minecraft.ModAchievements;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumInteraction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.net.AbstractPacket;

public class PacketToggleAI extends AbstractPacket<PacketToggleAI>
{
	private int entityId;
	private int interactionId;
	private List<Boolean> booleans;
	private List<Integer> integers;
	
	public PacketToggleAI()
	{
		//Required.
	}

	public PacketToggleAI(EntityVillagerMCA human, EnumInteraction interaction, Object... arguments)
	{
		this.entityId = human.getEntityId();
		this.interactionId = interaction.getId();
		this.booleans = new ArrayList<Boolean>();
		this.integers = new ArrayList<Integer>();
		
		for (Object obj : arguments)
		{
			if (obj.getClass() == Integer.class)
			{
				integers.add((Integer)obj);
			}
			
			else if (obj.getClass() == Boolean.class)
			{
				booleans.add((Boolean)obj);
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		booleans = new ArrayList<Boolean>();
		integers = new ArrayList<Integer>();
	
		entityId = byteBuf.readInt();
		interactionId = byteBuf.readInt();	
		
		int numBooleans = byteBuf.readInt();
		int numIntegers = byteBuf.readInt();
		
		while (numBooleans > 0)
		{
			booleans.add(byteBuf.readBoolean());
			numBooleans--;
		}
		
		while (numIntegers > 0)
		{
			integers.add(byteBuf.readInt());
			numIntegers--;
		}
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeInt(interactionId);
		byteBuf.writeInt(booleans.size());
		byteBuf.writeInt(integers.size());
		
		for (Boolean b : booleans)
		{
			byteBuf.writeBoolean(b);
		}
		
		for (Integer i : integers)
		{
			byteBuf.writeInt(i);
		}
	}

	@Override
	public void processOnGameThread(PacketToggleAI packet, MessageContext context) 
	{
		EntityPlayer player = getPlayer(context);
		EntityVillagerMCA human = (EntityVillagerMCA) player.worldObj.getEntityByID(packet.entityId);
		
		switch(EnumInteraction.fromId(packet.interactionId))
		{
		case FARMING: 
			player.addStat(ModAchievements.farming);
			human.getAI(AIFarming.class).startFarming(player, packet.integers.get(0), packet.integers.get(1), packet.booleans.get(0));
			break;
			
		case MINING: 
			player.addStat(ModAchievements.mining);
			
			if (packet.booleans.get(0))
			{
				human.getAI(AIMining.class).startGathering(player);
			}
			
			else
			{
				human.getAI(AIMining.class).startSearching(player, packet.integers.get(0));
			}
			break;
			
		case WOODCUTTING: 
			player.addStat(ModAchievements.woodcutting);
			boolean doReplant = packet.booleans.get(0);
			int mappingId = packet.integers.get(0);
			
			human.getAI(AIWoodcutting.class).startWoodcutting(player, mappingId, doReplant);
			break;
			
		case HUNTING: 
			player.addStat(ModAchievements.hunting);
			boolean flag = packet.booleans.get(0);
			
			if (flag)
			{
				human.getAI(AIHunting.class).startKilling(player);
			}
			
			else
			{
				human.getAI(AIHunting.class).startTaming(player);
			}
			
			break;
			
		case COOKING: 
			human.getAI(AICooking.class).startCooking(player);
			break;
			
		case FISHING:
			human.getAI(AIFishing.class).startFishing(player);
			break;
		}
	}
}

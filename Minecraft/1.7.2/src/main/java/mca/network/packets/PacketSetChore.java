package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.api.registries.ChoreRegistry;
import mca.chore.AbstractChore;
import mca.chore.ChoreCombat;
import mca.chore.ChoreCooking;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSetChore extends AbstractPacket implements IMessage, IMessageHandler<PacketSetChore, IMessage>
{
	private int entityId;
	private AbstractChore chore;

	public PacketSetChore()
	{
	}

	public PacketSetChore(int entityId, AbstractChore chore)
	{
		this.entityId = entityId;
		this.chore = chore;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		chore = (AbstractChore) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, chore);
	}

	@Override
	public IMessage onMessage(PacketSetChore packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(packet.entityId);
		packet.chore.owner = entity;

		if (entity != null)
		{
			if (packet.chore instanceof ChoreFarming)
			{
				entity.farmingChore = (ChoreFarming) packet.chore;
				entity.farmingChore.cropEntry = ChoreRegistry.getFarmingCropEntries().get(entity.farmingChore.entryIndex);
			}

			else if (packet.chore instanceof ChoreWoodcutting)
			{
				entity.woodcuttingChore = (ChoreWoodcutting) packet.chore;
				entity.woodcuttingChore.treeEntry = ChoreRegistry.getWoodcuttingTreeEntries().get(entity.woodcuttingChore.treeTypeIndex);
			}

			else if (packet.chore instanceof ChoreFishing)
			{
				entity.fishingChore = (ChoreFishing) packet.chore;
			}

			else if (packet.chore instanceof ChoreMining)
			{
				entity.miningChore = (ChoreMining) packet.chore;
				entity.miningChore.oreEntry = ChoreRegistry.getMiningOreEntries().get(entity.miningChore.entryIndex);
				entity.miningChore.searchBlock = entity.miningChore.oreEntry.getOreBlock();
			}

			else if (packet.chore instanceof ChoreCombat)
			{
				entity.combatChore = (ChoreCombat) packet.chore;
			}

			else if (packet.chore instanceof ChoreHunting)
			{
				entity.huntingChore = (ChoreHunting) packet.chore;
			}

			else if (packet.chore instanceof ChoreCooking)
			{
				entity.cookingChore = (ChoreCooking) packet.chore;
			}

			else
			{
				MCA.getInstance().getLogger().log("Unidentified chore type received when handling chore packet.");
			}
		}
		
		return null;
	}
}

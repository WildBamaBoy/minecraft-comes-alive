package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Constants;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketClickAid  extends AbstractPacket implements IMessage, IMessageHandler<PacketClickAid, IMessage>
{
	private int interactingEntityId;

	public PacketClickAid()
	{
	}

	public PacketClickAid(int interactingEntityId)
	{
		this.interactingEntityId = interactingEntityId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		interactingEntityId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(interactingEntityId);
	}

	@Override
	public IMessage onMessage(PacketClickAid packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.interactingEntityId);
		final ItemStack dropStack = null;

		Object[] giftInfo = null;

		if (entity.profession == 0)
		{
			giftInfo = Constants.farmerAidIDs[entity.worldObj.rand.nextInt(Constants.farmerAidIDs.length)];
		}

		else if (entity.profession == 4)
		{
			giftInfo = Constants.butcherAidIDs[entity.worldObj.rand.nextInt(Constants.butcherAidIDs.length)];
		}

		else
		{
			giftInfo = Constants.bakerAidIDs[entity.worldObj.rand.nextInt(Constants.bakerAidIDs.length)];
		}

		int quantityGiven = entity.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());
		entity.entityDropItem(new ItemStack((Item)giftInfo[0], quantityGiven), 0.2F);
		
		return null;
	}
}

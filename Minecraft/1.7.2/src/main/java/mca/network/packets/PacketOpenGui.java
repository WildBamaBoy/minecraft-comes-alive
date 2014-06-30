package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenGui extends AbstractPacket implements IMessage, IMessageHandler<PacketOpenGui, IMessage>
{
	private int interactingEntityId;
	private byte guiId;
	
	public PacketOpenGui()
	{
	}

	public PacketOpenGui(int interactingEntityId, byte guiId)
	{
		this.interactingEntityId = interactingEntityId;
		this.guiId = guiId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		interactingEntityId = byteBuf.readInt();
		guiId = byteBuf.readByte();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(interactingEntityId);
		byteBuf.writeByte(guiId);
	}

	@Override
	public IMessage onMessage(PacketOpenGui packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final Entity entity = player.worldObj.getEntityByID(packet.interactingEntityId);

		if (guiId == Constants.ID_GUI_SETUP && MCA.getInstance().hasReceivedClientSetup)
		{
			return null;
		}

		else if (guiId == Constants.ID_GUI_SETUP && !MCA.getInstance().hasReceivedClientSetup)
		{
			MCA.getInstance().hasReceivedClientSetup = true;
		}

		player.openGui(MCA.getInstance(), packet.guiId, player.worldObj, (int)entity.posX, (int)entity.posY, (int)entity.posZ);
		return null;
	}
}

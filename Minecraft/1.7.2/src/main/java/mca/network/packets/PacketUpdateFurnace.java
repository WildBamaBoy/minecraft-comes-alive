package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.entity.AbstractEntity;
import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateFurnace extends AbstractPacket implements IMessage, IMessageHandler<PacketUpdateFurnace, IMessage>
{
	private int entityId;
	private boolean state;
	
	public PacketUpdateFurnace()
	{
	}
	
	public PacketUpdateFurnace(int entityId, boolean state)
	{
		this.entityId = entityId;
		this.state = state;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		state = byteBuf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeBoolean(state);
	}

	@Override
	public IMessage onMessage(PacketUpdateFurnace packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

		BlockFurnace.updateFurnaceBlockState(packet.state, entity.worldObj, entity.cookingChore.furnacePosX, entity.cookingChore.furnacePosY, entity.cookingChore.furnacePosZ);
		
		return null;
	}
}

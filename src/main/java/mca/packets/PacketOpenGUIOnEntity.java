package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.packets.AbstractPacket;

public class PacketOpenGUIOnEntity extends AbstractPacket implements IMessage, IMessageHandler<PacketOpenGUIOnEntity, IMessage>
{
	private int entityId;
	private int guiId;
	
	public PacketOpenGUIOnEntity()
	{
	}

	public PacketOpenGUIOnEntity(int entityId, int guiId)
	{
		this.entityId = entityId;
		this.guiId = guiId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		entityId = byteBuf.readInt();
		guiId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeInt(guiId);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketOpenGUIOnEntity packet, MessageContext context)
	{
		final EntityPlayer player = this.getPlayer(context);
		final EntityHuman entity = (EntityHuman) player.worldObj.getEntityByID(packet.entityId);
		
		player.openGui(MCA.getInstance(), packet.guiId, player.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ);
		
		return null;
	}
}

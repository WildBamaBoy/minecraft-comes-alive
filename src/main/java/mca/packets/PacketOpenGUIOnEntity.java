package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiInteraction;
import mca.entity.EntityHuman;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.packets.AbstractPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketOpenGUIOnEntity extends AbstractPacket implements IMessage, IMessageHandler<PacketOpenGUIOnEntity, IMessage>
{
	private int entityId;

	public PacketOpenGUIOnEntity()
	{
	}

	public PacketOpenGUIOnEntity(int entityId)
	{
		this.entityId = entityId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		entityId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(entityId);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketOpenGUIOnEntity packet, MessageContext context)
	{
		final EntityPlayer player = this.getPlayer(context);
		final EntityHuman entity = (EntityHuman) player.worldObj.getEntityByID(packet.entityId);
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiInteraction(entity, player));
		return null;
	}
}

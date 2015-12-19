package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiVillagerEditor;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.items.ItemVillagerEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketOpenGUIOnEntity packet = (PacketOpenGUIOnEntity)message;
		final EntityPlayer player = this.getPlayer(context);
		final EntityHuman entity = (EntityHuman) player.worldObj.getEntityByID(packet.entityId);
		
		if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemVillagerEditor)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiVillagerEditor(entity, player));
		}
		
		else
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiInteraction(entity, player));			
		}
	}
}

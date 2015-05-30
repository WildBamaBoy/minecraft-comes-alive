package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiVillagerEditor;
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
		
		if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemVillagerEditor)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiVillagerEditor(entity, player));
		}
		
		else
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiInteraction(entity, player));			
		}
		
		return null;
	}
}

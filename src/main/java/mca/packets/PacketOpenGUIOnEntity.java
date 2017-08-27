package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiInteraction;
import mca.client.gui.GuiInventory;
import mca.client.gui.GuiVillagerEditor;
import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemVillagerEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.modules.net.AbstractPacket;

public class PacketOpenGUIOnEntity extends AbstractPacket<PacketOpenGUIOnEntity>
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
	public void processOnGameThread(PacketOpenGUIOnEntity packet, MessageContext context) 
	{
		//Only open the GUI if the player doesn't have a previous one open.
		if (Minecraft.getMinecraft().currentScreen == null)
		{
			final EntityPlayer player = this.getPlayer(context);
			final EntityVillagerMCA entity = (EntityVillagerMCA) player.world.getEntityByID(packet.entityId);
			
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
}

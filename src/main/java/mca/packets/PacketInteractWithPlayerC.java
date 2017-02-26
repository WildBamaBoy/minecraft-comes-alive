package mca.packets;

import org.lwjgl.input.Keyboard;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiPlayerMenu;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMarriageState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketInteractWithPlayerC extends AbstractPacket<PacketInteractWithPlayerC>
{
	private int targetEntityId;
	private boolean targetIsMarried;
	private boolean targetIsEngaged;
	private boolean isMarriedToInitiator;
	private String targetSpouseName;
	
	public PacketInteractWithPlayerC()
	{
	}

	public PacketInteractWithPlayerC(EntityPlayer initiator, EntityPlayer target)
	{
		this.targetEntityId = target.getEntityId();
		
		NBTPlayerData initData = MCA.getPlayerData(initiator);
		NBTPlayerData targetData = MCA.getPlayerData(target);
		
		targetIsMarried = targetData.getSpouseUUID() != Constants.EMPTY_UUID;
		targetIsEngaged = targetData.getMarriageState() == EnumMarriageState.ENGAGED;
		isMarriedToInitiator = targetData.getSpouseUUID() == initData.getUUID();
		
		for (Object obj : initiator.world.loadedEntityList)
		{
			if (obj instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA human = (EntityVillagerMCA)obj;
				
				if (human.attributes.getSpouseUUID() == targetData.getUUID())
				{
					targetSpouseName = human.attributes.getName();
				}
			}
		}
		
		if (targetSpouseName == null || targetSpouseName.isEmpty())
		{
			targetSpouseName = "?";
		}
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		targetEntityId = byteBuf.readInt();
		targetIsMarried = byteBuf.readBoolean();
		targetIsEngaged = byteBuf.readBoolean();
		isMarriedToInitiator = byteBuf.readBoolean();
		targetSpouseName = (String) RadixNettyIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(targetEntityId);
		byteBuf.writeBoolean(targetIsMarried);
		byteBuf.writeBoolean(targetIsEngaged);
		byteBuf.writeBoolean(isMarriedToInitiator);
		RadixNettyIO.writeObject(byteBuf, targetSpouseName);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void processOnGameThread(PacketInteractWithPlayerC packet, MessageContext context) 
	{
		EntityPlayer recipient = this.getPlayerClient();
		EntityPlayer target = (EntityPlayer) recipient.world.getEntityByID(packet.targetEntityId);
		
		if (target != null)
		{
			if (MCA.getConfig().shiftClickForPlayerMarriage && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				return;
			}
			
			Minecraft.getMinecraft().displayGuiScreen(new GuiPlayerMenu(recipient, target, packet.targetIsMarried, packet.targetIsEngaged, packet.isMarriedToInitiator, packet.targetSpouseName));
		}
		
	}
}

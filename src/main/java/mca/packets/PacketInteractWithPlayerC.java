package mca.packets;

import org.lwjgl.input.Keyboard;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiPlayerMenu;
import mca.core.MCA;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

public class PacketInteractWithPlayerC extends AbstractPacket implements IMessage, IMessageHandler<PacketInteractWithPlayerC, IMessage>
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
		
		PlayerData initData = MCA.getPlayerData(initiator);
		PlayerData targetData = MCA.getPlayerData(target);
		
		targetIsMarried = targetData.spousePermanentId.getInt() != 0;
		targetIsEngaged = targetData.isEngaged.getBoolean();
		isMarriedToInitiator = targetData.spousePermanentId.getInt() == initData.permanentId.getInt();
		
		for (Object obj : initiator.worldObj.loadedEntityList)
		{
			if (obj instanceof EntityHuman)
			{
				EntityHuman human = (EntityHuman)obj;
				
				if (human.getSpouseId() == targetData.permanentId.getInt())
				{
					targetSpouseName = human.getName();
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
		targetSpouseName = (String) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(targetEntityId);
		byteBuf.writeBoolean(targetIsMarried);
		byteBuf.writeBoolean(targetIsEngaged);
		byteBuf.writeBoolean(isMarriedToInitiator);
		ByteBufIO.writeObject(byteBuf, targetSpouseName);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketInteractWithPlayerC packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketInteractWithPlayerC packet = (PacketInteractWithPlayerC) message;
		
		EntityPlayer recipient = this.getPlayerClient();
		EntityPlayer target = (EntityPlayer) recipient.worldObj.getEntityByID(packet.targetEntityId);
		
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

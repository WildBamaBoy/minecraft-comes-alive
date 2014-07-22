package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.item.AbstractBaby;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class PacketOnPlayerProcreate extends AbstractPacket implements IMessage, IMessageHandler<PacketOnPlayerProcreate, IMessage>
{
	private int spouseEntityId;
	private int initiatingPlayerEntityId;

	public PacketOnPlayerProcreate()
	{
	}

	public PacketOnPlayerProcreate(int spouseEntityId, int initiatingPlayerEntityId)
	{
		this.spouseEntityId = spouseEntityId;
		this.initiatingPlayerEntityId = initiatingPlayerEntityId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		this.spouseEntityId = byteBuf.readInt();
		this.initiatingPlayerEntityId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(spouseEntityId);
		byteBuf.writeInt(initiatingPlayerEntityId);
	}

	@Override
	public IMessage onMessage(PacketOnPlayerProcreate packet, MessageContext context) 
	{
		if (context.side == Side.CLIENT)
		{
			final EntityPlayer sender = packet.getPlayer(context);
			final EntityPlayer spouse = (EntityPlayer)sender.worldObj.getEntityByID(packet.spouseEntityId);

			//Trigger the name baby gui.
			AbstractBaby itemBaby = null;
			boolean babyIsMale = Utility.getRandomGender();

			if (babyIsMale)
			{
				itemBaby = (AbstractBaby)MCA.getInstance().itemBabyBoy;
			}

			else
			{
				itemBaby = (AbstractBaby)MCA.getInstance().itemBabyGirl;
			}

			WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(sender.getCommandSenderName());
			MCA.getInstance().getWorldProperties(manager).babyIsMale = babyIsMale;
			MCA.getInstance().getWorldProperties(manager).babyExists = true;
			manager.saveWorldProperties();

			MCA.packetHandler.sendPacketToServer(new PacketAddBaby(babyIsMale));
			sender.openGui(MCA.getInstance(), Constants.ID_GUI_NAMECHILD, sender.worldObj, (int)sender.posX, (int)sender.posY, (int)sender.posZ);
		}
		
		return null;
	}
}

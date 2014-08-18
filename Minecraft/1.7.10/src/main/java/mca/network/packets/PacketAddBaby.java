package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.item.ItemBabyBoy;
import mca.item.ItemBabyGirl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketAddBaby  extends AbstractPacket implements IMessage, IMessageHandler<PacketAddBaby, IMessage>
{
	private boolean isMale;
	
	public PacketAddBaby()
	{
	}
	
	public PacketAddBaby(boolean isMale)
	{
		this.isMale = isMale;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		isMale = byteBuf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeBoolean(isMale);
	}

	@Override
	public IMessage onMessage(PacketAddBaby packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		
		final Item itemToAdd = packet.isMale ? MCA.getInstance().itemBabyBoy : MCA.getInstance().itemBabyGirl;
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		EntityPlayer spousePlayer = null;

		//Check for spouse.
		if (manager != null && MCA.getInstance().getWorldProperties(manager).playerSpouseID < 0)
		{
			spousePlayer = player.worldObj.getPlayerEntityByName(MCA.getInstance().getWorldProperties(manager).playerSpouseName);
		}

		player.inventory.addItemStackToInventory(new ItemStack(itemToAdd));

		if (itemToAdd instanceof ItemBabyBoy)
		{
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);

			if (spousePlayer != null)
			{
				spousePlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);	
			}
		}

		else if (itemToAdd instanceof ItemBabyGirl)
		{
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);

			if (spousePlayer != null)
			{
				spousePlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyGirl);
			}
		}
		
		return null;
	}
}

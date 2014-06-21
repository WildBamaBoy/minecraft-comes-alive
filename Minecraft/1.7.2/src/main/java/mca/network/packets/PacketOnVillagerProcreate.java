package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.entity.AbstractEntity;
import mca.item.AbstractBaby;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketOnVillagerProcreate extends AbstractPacket implements IMessage, IMessageHandler<PacketOnVillagerProcreate, IMessage>
{
	private int entityId;
	private boolean babyIsMale;
	
	public PacketOnVillagerProcreate()
	{
	}

	public PacketOnVillagerProcreate(int entityId, boolean babyIsMale)
	{
		this.entityId = entityId;
		this.babyIsMale = babyIsMale;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		this.entityId = byteBuf.readInt();
		this.babyIsMale = byteBuf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeBoolean(babyIsMale);
	}

	@Override
	public IMessage onMessage(PacketOnVillagerProcreate packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		AbstractEntity villager = (AbstractEntity)player.worldObj.getEntityByID(packet.entityId);
		AbstractBaby itemBaby = null;

		//Unlock the appropriate achievement.
		if (babyIsMale)
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyBoy;
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);
		}

		else
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyGirl;
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyGirl);
		}

		//Give the baby to the villager.
		villager.inventory.addItemStackToInventory(new ItemStack(itemBaby, 1));
		MCA.packetHandler.sendPacketToServer(new PacketSetInventory(packet.entityId, villager.inventory));

		//Modify the player's world properties manager.
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		manager.worldProperties.babyIsMale = babyIsMale;
		manager.worldProperties.babyExists = true;
		manager.saveWorldProperties();

		//Make the player choose a name for the baby.
		player.openGui(MCA.getInstance(), Constants.ID_GUI_NAMECHILD, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
		return null;
	}
}

package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.actions.ActionProcreate;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemBaby;
import mca.util.TutorialManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketBabyName extends AbstractPacket<PacketBabyName>
{
	private String babyName;
	private int slot;
	
	public PacketBabyName()
	{
	}

	public PacketBabyName(String babyName, int slot)
	{
		this.babyName = babyName;
		this.slot = slot;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		babyName = (String) RadixNettyIO.readObject(byteBuf);
		slot = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		RadixNettyIO.writeObject(byteBuf, babyName);
		byteBuf.writeInt(slot);
	}

	@Override
	public void processOnGameThread(PacketBabyName packet, MessageContext context) 
	{
		EntityPlayer senderPlayer = this.getPlayer(context);
		ItemStack stack = packet.slot == -1 ? null : senderPlayer.inventory.getStackInSlot(packet.slot); //To avoid index out of bounds.
		NBTPlayerData data = MCA.getPlayerData(senderPlayer);
		EntityVillagerMCA playerSpouse = (EntityVillagerMCA)MCA.getEntityByUUID(senderPlayer.world, data.getSpouseUUID());
		
		//Player has the baby.
		if (stack != null && stack.getItem() instanceof ItemBaby)
		{
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.setString("name", packet.babyName);
		}
		
		//Player's spouse will have the baby if stack is null.
		else if (stack == null)
		{
			if (playerSpouse != null)
			{
				int babySlot = playerSpouse.attributes.getInventory().getFirstSlotContainingItem(ItemsMCA.BABY_BOY);
				babySlot = babySlot == -1 ? playerSpouse.attributes.getInventory().getFirstSlotContainingItem(ItemsMCA.BABY_GIRL) : babySlot;
				
				if (babySlot != -1)
				{
					playerSpouse.attributes.getInventory().getStackInSlot(babySlot).getTagCompound().setString("name", packet.babyName);
				}
			}
		}
		
		//Random chance for twins.
		if (RadixLogic.getBooleanWithProbability(MCA.getConfig().chanceToHaveTwins))
		{
			if (playerSpouse != null)
			{
				final ActionProcreate procreateAI = playerSpouse.getBehavior(ActionProcreate.class);
				
				if (!procreateAI.getHasHadTwins())
				{
					playerSpouse.getBehavior(ActionProcreate.class).setIsProcreating(true);
					procreateAI.setHasHadTwins(true);
					//senderPlayer.addStat(AchievementsMCA.twins);
					
					TutorialManager.sendMessageToPlayer(senderPlayer, "Congratulations! You've just had twins!", "Your spouse can only have twins once.");
				}
			}
		}
	}
}

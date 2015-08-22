package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.ai.AIProcreate;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import mca.items.ItemBaby;
import mca.util.TutorialManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import radixcore.util.RadixLogic;

public class PacketBabyName extends AbstractPacket implements IMessage, IMessageHandler<PacketBabyName, IMessage>
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
		babyName = (String) ByteBufIO.readObject(byteBuf);
		slot = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		ByteBufIO.writeObject(byteBuf, babyName);
		byteBuf.writeInt(slot);
	}

	@Override
	public IMessage onMessage(PacketBabyName packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketBabyName packet = (PacketBabyName)message;
		EntityPlayer senderPlayer = this.getPlayer(context);
		ItemStack stack = packet.slot == -1 ? null : senderPlayer.inventory.getStackInSlot(packet.slot); //To avoid index out of bounds.
		PlayerData data = MCA.getPlayerData(senderPlayer);
		EntityHuman playerSpouse = MCA.getHumanByPermanentId(data.spousePermanentId.getInt());
		
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
				int babySlot = playerSpouse.getVillagerInventory().getFirstSlotContainingItem(ModItems.babyBoy);
				babySlot = babySlot == -1 ? playerSpouse.getVillagerInventory().getFirstSlotContainingItem(ModItems.babyGirl) : babySlot;
				
				if (babySlot != -1)
				{
					playerSpouse.getVillagerInventory().getStackInSlot(babySlot).getTagCompound().setString("name", packet.babyName);
				}
			}
		}
		
		//Random chance for twins.
		if (RadixLogic.getBooleanWithProbability(MCA.getConfig().chanceToHaveTwins))
		{
			if (playerSpouse != null)
			{
				final AIProcreate procreateAI = playerSpouse.getAI(AIProcreate.class);
				
				if (!procreateAI.getHasHadTwins())
				{
					playerSpouse.getAI(AIProcreate.class).setIsProcreating(true);
					procreateAI.setHasHadTwins(true);
					senderPlayer.triggerAchievement(ModAchievements.twins);
					
					TutorialManager.sendMessageToPlayer(senderPlayer, "Congratulations! You've just had twins!", "Your spouse can only have twins once.");
				}
			}
		}
	}
}

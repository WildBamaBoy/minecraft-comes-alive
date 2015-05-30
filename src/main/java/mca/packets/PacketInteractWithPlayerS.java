package mca.packets;

import io.netty.buffer.ByteBuf;

import java.util.Random;

import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import mca.enums.EnumInteraction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.constant.Font.Color;
import radixcore.packets.AbstractPacket;

public class PacketInteractWithPlayerS extends AbstractPacket implements IMessage, IMessageHandler<PacketInteractWithPlayerS, IMessage>
{
	private int interactionId;
	private int entityId;

	public PacketInteractWithPlayerS()
	{
	}

	public PacketInteractWithPlayerS(int interactionId, int entityId)
	{
		this.interactionId = interactionId;
		this.entityId = entityId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.interactionId = byteBuf.readInt();
		this.entityId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(interactionId);
		byteBuf.writeInt(entityId);
	}

	@Override
	public IMessage onMessage(PacketInteractWithPlayerS packet, MessageContext context)
	{
		EntityPlayer sender = this.getPlayer(context);
		EntityPlayer target = (EntityPlayer) sender.worldObj.getEntityByID(packet.entityId);
		PlayerData senderData = MCA.getPlayerData(sender);
		PlayerData targetData = MCA.getPlayerData(target);
		EnumInteraction interaction = EnumInteraction.fromId(packet.interactionId);

		boolean senderHasWeddingRing = false;

		for (ItemStack stack : sender.inventory.mainInventory)
		{
			if (stack != null)
			{
				Item item = stack.getItem();

				if (item == ModItems.weddingRing || item == ModItems.weddingRingRG)
				{
					senderHasWeddingRing = true;
				}
			}
		}

		switch (interaction)
		{
		case ASKTOMARRY:
			if (targetData.spousePermanentId.getInt() != 0 || targetData.isEngaged.getBoolean())
			{
				sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString("interactionp.marry.fail.targetalreadymarried", target.getName())));
			}

			else if (senderData.spousePermanentId.getInt() != 0 || senderData.isEngaged.getBoolean())
			{
				sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString("interactionp.marry.fail.alreadymarried")));				
			}

			else if (!senderHasWeddingRing)
			{
				sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString("interactionp.marry.fail.noweddingring")));
			}

			else
			{
				sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString("interactionp.marry.sent", target.getName())));
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenPrompt(sender, target, interaction), (EntityPlayerMP)target);
			}

			break;
		case DIVORCE:
			targetData.setNotMarried();
			senderData.setNotMarried();

			sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString(Color.RED + MCA.getLanguageManager().getString("interactionp.divorce.notify", target.getName()))));
			target.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString(Color.RED + MCA.getLanguageManager().getString("interactionp.divorce.notify", sender.getName()))));
			break;

		case HAVEBABY:
			if (senderData.shouldHaveBaby.getBoolean())
			{
				sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString("interactionp.havebaby.fail.alreadyexists", target.getName())));				
			}

			else
			{
				sender.addChatMessage(new ChatComponentText(MCA.getLanguageManager().getString("interactionp.havebaby.sent", target.getName())));
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenPrompt(sender, target, interaction), (EntityPlayerMP)target);
			}
			
			break;

		case ASKTOMARRY_ACCEPT:
			sender.addChatMessage(new ChatComponentText(Color.GREEN + MCA.getLanguageManager().getString("interactionp.marry.success", target.getName())));
			target.addChatMessage(new ChatComponentText(Color.GREEN + MCA.getLanguageManager().getString("interactionp.marry.success", sender.getName())));

			senderData.setMarried(target);
			targetData.setMarried(sender);

			for (int i = 0; i < target.inventory.getSizeInventory(); i++)
			{
				ItemStack stack = target.inventory.getStackInSlot(i);

				if (stack != null)
				{
					if (stack.getItem() == ModItems.weddingRing || stack.getItem() == ModItems.weddingRingRG)
					{
						target.inventory.consumeInventoryItem(stack.getItem());
						break;
					}
				}
			}

			break;

		case HAVEBABY_ACCEPT:
			senderData.shouldHaveBaby.setValue(true);
			targetData.shouldHaveBaby.setValue(true);

			boolean isMale = new Random().nextBoolean();
			ItemStack stack = new ItemStack(isMale ? ModItems.babyBoy : ModItems.babyGirl);
			target.inventory.addItemStackToInventory(stack);

			Achievement achievement = isMale ? ModAchievements.babyBoy : ModAchievements.babyGirl;
			sender.triggerAchievement(achievement);
			target.triggerAchievement(achievement);

			MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenBabyNameGUI(isMale), (EntityPlayerMP) target);

			break;
		}
		return null;
	}
}

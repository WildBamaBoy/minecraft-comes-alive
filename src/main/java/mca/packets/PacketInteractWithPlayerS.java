package mca.packets;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.NBTPlayerData;
import mca.enums.EnumInteraction;
import mca.util.MarriageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.text.TextComponentString;
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
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketInteractWithPlayerS packet = (PacketInteractWithPlayerS)message;
		EntityPlayer sender = this.getPlayer(context);
		EntityPlayer target = (EntityPlayer) sender.worldObj.getEntityByID(packet.entityId);
		NBTPlayerData senderData = MCA.getPlayerData(sender);
		NBTPlayerData targetData = MCA.getPlayerData(target);
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
			if (targetData.getSpousePermanentId() != 0 || targetData.getIsEngaged())
			{
				sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString("interactionp.marry.fail.targetalreadymarried", target.getName())));
			}

			else if (senderData.getSpousePermanentId() != 0 || senderData.getIsEngaged())
			{
				sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString("interactionp.marry.fail.alreadymarried")));				
			}

			else if (!senderHasWeddingRing)
			{
				sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString("interactionp.marry.fail.noweddingring")));
			}

			else
			{
				sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString("interactionp.marry.sent", target.getName())));
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenPrompt(sender, target, interaction), (EntityPlayerMP)target);
			}

			break;
		case DIVORCE:
			MarriageHandler.endMarriage(sender, target);

			sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString(Color.RED + MCA.getLanguageManager().getString("interactionp.divorce.notify", target.getName()))));
			target.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString(Color.RED + MCA.getLanguageManager().getString("interactionp.divorce.notify", sender.getName()))));
			break;

		case HAVEBABY:
			if (senderData.getShouldHaveBaby())
			{
				sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString("interactionp.havebaby.fail.alreadyexists", target.getName())));				
			}

			else
			{
				sender.addChatMessage(new TextComponentString(MCA.getLanguageManager().getString("interactionp.havebaby.sent", target.getName())));
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenPrompt(sender, target, interaction), (EntityPlayerMP)target);
			}
			
			break;

		case ASKTOMARRY_ACCEPT:
			sender.addChatMessage(new TextComponentString(Color.GREEN + MCA.getLanguageManager().getString("interactionp.marry.success", target.getName())));
			target.addChatMessage(new TextComponentString(Color.GREEN + MCA.getLanguageManager().getString("interactionp.marry.success", sender.getName())));

			MarriageHandler.startMarriage(sender, target);

			for (int i = 0; i < target.inventory.getSizeInventory(); i++)
			{
				ItemStack stack = target.inventory.getStackInSlot(i);

				if (stack != null)
				{
					if (stack.getItem() == ModItems.weddingRing || stack.getItem() == ModItems.weddingRingRG)
					{
						target.inventory.deleteStack(stack);
						break;
					}
				}
			}

			break;

		case HAVEBABY_ACCEPT:
			senderData.setShouldHaveBaby(true);
			targetData.setShouldHaveBaby(true);

			boolean isMale = new Random().nextBoolean();
			ItemStack stack = new ItemStack(isMale ? ModItems.babyBoy : ModItems.babyGirl);
			target.inventory.addItemStackToInventory(stack);

			Achievement achievement = isMale ? ModAchievements.babyBoy : ModAchievements.babyGirl;
			sender.addStat(achievement);
			target.addStat(achievement);

			MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenBabyNameGUI(isMale), (EntityPlayerMP) target);

			break;
		}
	}
}

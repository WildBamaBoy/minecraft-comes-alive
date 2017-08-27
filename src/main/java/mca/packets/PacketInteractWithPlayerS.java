package mca.packets;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumInteraction;
import mca.enums.EnumMarriageState;
import mca.util.Either;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.constant.Font.Color;
import radixcore.modules.net.AbstractPacket;

public class PacketInteractWithPlayerS extends AbstractPacket<PacketInteractWithPlayerS>
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
	public void processOnGameThread(PacketInteractWithPlayerS packet, MessageContext context) 
	{
		EntityPlayer sender = this.getPlayer(context);
		EntityPlayer target = (EntityPlayer) sender.world.getEntityByID(packet.entityId);
		NBTPlayerData senderData = MCA.getPlayerData(sender);
		NBTPlayerData targetData = MCA.getPlayerData(target);
		EnumInteraction interaction = EnumInteraction.fromId(packet.interactionId);

		boolean senderHasWeddingRing = false;

		for (ItemStack stack : sender.inventory.mainInventory)
		{
			if (stack != null)
			{
				Item item = stack.getItem();

				if (item == ItemsMCA.WEDDING_RING || item == ItemsMCA.WEDDING_RING_RG)
				{
					senderHasWeddingRing = true;
				}
			}
		}

		switch (interaction)
		{
		case ASKTOMARRY:
			if (targetData.getSpouseUUID() != Constants.EMPTY_UUID || senderData.getMarriageState() != EnumMarriageState.NOT_MARRIED)
			{
				sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interactionp.marry.fail.targetalreadymarried", target.getName())));
			}

			else if (senderData.getSpouseUUID() != Constants.EMPTY_UUID || senderData.getMarriageState() != EnumMarriageState.NOT_MARRIED)
			{
				sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interactionp.marry.fail.alreadymarried")));				
			}

			else if (!senderHasWeddingRing)
			{
				sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interactionp.marry.fail.noweddingring")));
			}

			else
			{
				sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interactionp.marry.sent", target.getName())));
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenPrompt(sender, target, interaction), (EntityPlayerMP)target);
			}

			break;
		case DIVORCE:
			senderData.setSpouse(null);
			targetData.setSpouse(null);
			
			sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString(Color.RED + MCA.getLocalizer().getString("interactionp.divorce.notify", target.getName()))));
			target.sendMessage(new TextComponentString(MCA.getLocalizer().getString(Color.RED + MCA.getLocalizer().getString("interactionp.divorce.notify", sender.getName()))));
			break;

		case HAVEBABY:
			if (senderData.getOwnsBaby())
			{
				sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interactionp.havebaby.fail.alreadyexists", target.getName())));				
			}

			else
			{
				sender.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interactionp.havebaby.sent", target.getName())));
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenPrompt(sender, target, interaction), (EntityPlayerMP)target);
			}
			
			break;

		case ASKTOMARRY_ACCEPT:
			sender.sendMessage(new TextComponentString(Color.GREEN + MCA.getLocalizer().getString("interactionp.marry.success", target.getName())));
			target.sendMessage(new TextComponentString(Color.GREEN + MCA.getLocalizer().getString("interactionp.marry.success", sender.getName())));

			senderData.setSpouse(Either.<EntityVillagerMCA, EntityPlayer>withR(target));

			for (int i = 0; i < target.inventory.getSizeInventory(); i++)
			{
				ItemStack stack = target.inventory.getStackInSlot(i);

				if (stack != null)
				{
					if (stack.getItem() == ItemsMCA.WEDDING_RING || stack.getItem() == ItemsMCA.WEDDING_RING_RG)
					{
						target.inventory.deleteStack(stack);
						break;
					}
				}
			}

			break;

		case HAVEBABY_ACCEPT:
			senderData.setOwnsBaby(true);
			targetData.setOwnsBaby(true);

			boolean isMale = new Random().nextBoolean();
			ItemStack stack = new ItemStack(isMale ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
			target.inventory.addItemStackToInventory(stack);

			//Achievement achievement = isMale ? AchievementsMCA.babyBoy : AchievementsMCA.babyGirl;
			//sender.addStat(achievement);
			//target.addStat(achievement);
			
			MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenBabyNameGUI(isMale), (EntityPlayerMP) target);

			break;
		}
	}
}

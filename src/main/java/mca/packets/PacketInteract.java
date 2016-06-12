package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.ai.AIMood;
import mca.ai.AIProcreate;
import mca.ai.AISleep;
import mca.api.RegistryMCA;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumDialogueType;
import mca.enums.EnumInteraction;
import mca.enums.EnumPersonality;
import mca.items.ItemBaby;
import mca.util.MarriageHandler;
import mca.util.TutorialManager;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class PacketInteract extends AbstractPacket implements IMessage, IMessageHandler<PacketInteract, IMessage>
{
	private int buttonId;
	private int entityId;
	private int numAdditionalData;
	private Object[] additionalData;

	public PacketInteract()
	{
	}

	public PacketInteract(int buttonId, int entityId)
	{
		this.buttonId = buttonId;
		this.entityId = entityId;
	}

	public PacketInteract(int buttonId, int entityId, Object... additionalData)
	{
		this.buttonId = buttonId;
		this.entityId = entityId;
		this.numAdditionalData = additionalData.length;
		this.additionalData = additionalData;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		this.buttonId = byteBuf.readInt();
		this.entityId = byteBuf.readInt();
		this.numAdditionalData = byteBuf.readInt();
		this.additionalData = new Object[numAdditionalData];

		for (int i = 0; i < this.numAdditionalData; i++)
		{
			additionalData[i] = ByteBufIO.readObject(byteBuf);
		}
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(buttonId);
		byteBuf.writeInt(entityId);
		byteBuf.writeInt(numAdditionalData);

		if (numAdditionalData > 0)
		{
			for (Object obj : additionalData)
			{
				ByteBufIO.writeObject(byteBuf, obj);
			}
		}
	}

	@Override
	public IMessage onMessage(PacketInteract packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketInteract packet = (PacketInteract)message;
		EntityHuman villager = null;
		EntityPlayer player = null;

		for (WorldServer world : MinecraftServer.getServer().worldServers)
		{
			player = getPlayer(context);
			villager = (EntityHuman) world.getEntityByID(packet.entityId);

			if (player != null && villager != null)
			{
				break;
			}
		}

		if (player != null && villager != null)
		{
			EnumInteraction interaction = EnumInteraction.fromId(packet.buttonId);

			if (interaction == EnumInteraction.SET_HOME)
			{
				if (villager.getAI(AISleep.class).setHomePoint(villager.posX, villager.posY, villager.posZ))
				{
					villager.say("interaction.sethome.success", player);
					TutorialManager.sendMessageToPlayer(player, "Villagers go to their home points at night, and then go to sleep.", "If their home point becomes blocked, they will automatically find a new one.");
				}

				else
				{
					Block block = null;
					int iPosX = (int)villager.posX;
					int iPosY = (int)villager.posY;
					int iPosZ = (int)villager.posZ;

					if (!Utilities.isPointClear(villager.worldObj, iPosX, iPosY, iPosZ))
					{
						block = BlockHelper.getBlock(villager.worldObj, iPosX, iPosY, iPosZ);
					}

					else if (!Utilities.isPointClear(villager.worldObj, iPosX, iPosY + 1, iPosZ))
					{
						block = BlockHelper.getBlock(villager.worldObj, iPosX, iPosY + 1, iPosZ);
					}

					if (block != null)
					{
						villager.say("interaction.sethome.fail", player, block.getLocalizedName().toLowerCase());
					}

					TutorialManager.sendMessageToPlayer(player, "Move villagers away from the edges of walls", "and other blocks before setting their home.");
				}
			}

			else if (interaction == EnumInteraction.TRADE)
			{
				villager.setCustomer(player);
				player.displayVillagerTradeGui(villager);
			}

			else if (interaction == EnumInteraction.PICK_UP)
			{
				villager.mountEntity(player);
			}

			else if (interaction == EnumInteraction.TAKE_GIFT)
			{
				PlayerMemory memory = villager.getPlayerMemory(player);
				memory.setHasGift(false);

				ItemStack stack = RegistryMCA.getGiftStackFromRelationship(memory.getHearts());
				villager.dropItem(stack.getItem(), stack.stackSize);
			}

			else if (interaction == EnumInteraction.CHAT || interaction == EnumInteraction.JOKE || interaction == EnumInteraction.SHAKE_HAND ||
					interaction == EnumInteraction.TELL_STORY || interaction == EnumInteraction.FLIRT || interaction == EnumInteraction.HUG ||
					interaction == EnumInteraction.KISS)
			{
				AIMood mood = villager.getAI(AIMood.class);
				PlayerMemory memory = villager.getPlayerMemory(player);

				//First check for spouse leaving due to low hearts.
				if (memory.getDialogueType() == EnumDialogueType.SPOUSE && memory.getHearts() <= 25 && villager.timesWarnedForLowHearts >= 3)
				{
					villager.say("spouse.endmarriage", player, player);
					player.addChatComponentMessage(new ChatComponentText(Color.RED + MCA.getLanguageManager().getString("notify.spouseendedmarriage", villager)));
					memory.setHearts(-100);
					mood.modifyMoodLevel(-20.0F);
					villager.timesWarnedForLowHearts = 0;

					MarriageHandler.endMarriage(player, villager);
				}

				else
				{
					int successChance = interaction.getSuccessChance(villager, memory);

					int pointsModification = interaction.getBasePoints()
							+ villager.getPersonality().getHeartsModifierForInteraction(interaction) 
							+ mood.getMood(villager.getPersonality()).getPointsModifierForInteraction(interaction);

					boolean wasGood = RadixLogic.getBooleanWithProbability(successChance);

					if (villager.getPersonality() == EnumPersonality.FRIENDLY)
					{
						pointsModification += pointsModification * 0.15D;
					}

					else if (villager.getPersonality() == EnumPersonality.FLIRTY)
					{
						pointsModification += pointsModification * 0.25D;
					}

					else if (villager.getPersonality() == EnumPersonality.SENSITIVE && RadixLogic.getBooleanWithProbability(5))
					{
						pointsModification = -35;
						wasGood = false;
					}

					else if (villager.getPersonality() == EnumPersonality.STUBBORN)
					{
						pointsModification -= pointsModification * 0.15D;
					}

					if (wasGood)
					{
						pointsModification = RadixMath.clamp(pointsModification, 1, 100);
						mood.modifyMoodLevel(RadixMath.getNumberInRange(0.2F, 1.0F));
						villager.say(memory.getDialogueType().toString() + "." + interaction.getName() + ".good", player);
					}

					else
					{
						pointsModification = RadixMath.clamp(pointsModification * -1, -100, -1);
						mood.modifyMoodLevel(RadixMath.getNumberInRange(0.2F, 1.0F) * -1);
						villager.say(memory.getDialogueType().toString() + "." + interaction.getName() + ".bad", player);
					}

					memory.setHearts(memory.getHearts() + pointsModification);
					memory.increaseInteractionFatigue();

					if (memory.getHearts() >= 100)
					{
						player.triggerAchievement(ModAchievements.fullGoldHearts);
					}

					if (memory.getInteractionFatigue() == 4)
					{
						TutorialManager.sendMessageToPlayer(player, "Villagers tire of conversation after a few tries.", "Talk to them later for better success chances.");
					}
				}
			}

			else if (interaction == EnumInteraction.STOP)
			{
				villager.getAIManager().disableAllToggleAIs();
			}

			else if (interaction == EnumInteraction.INVENTORY)
			{
				villager.openInventory(player);
			}

			else if (interaction == EnumInteraction.RIDE_HORSE)
			{
				if (villager.ridingEntity != null)
				{
					//horseSaddled is set to false when mounted by a villager in order for
					//the navigator to function properly and make them move. Set them back
					//as saddled when the villager dismounts.
					EntityHorse horse = (EntityHorse)villager.ridingEntity;
					horse.setHorseSaddled(true);

					villager.mountEntity(null);
				}

				else
				{
					EntityHorse horse = (EntityHorse)RadixLogic.getNearestEntityOfTypeWithinDistance(EntityHorse.class, villager, 5);

					if (horse != null)
					{
						if (horse.isHorseSaddled() && horse.riddenByEntity == null)
						{
							villager.mountEntity(horse);
						}

						else
						{
							villager.say("interaction.ridehorse.fail.notrideable", player);
						}
					}

					else
					{
						villager.say("interaction.ridehorse.fail.notnearby", player);
					}
				}
			}

			else if (interaction == EnumInteraction.DIVORCE)
			{
				PlayerData data = MCA.getPlayerData(player);

				if (data.getSpousePermanentId() != 0)
				{
					villager.say("interaction.divorce.priest.success", player);

					EntityHuman spouse = MCA.getHumanByPermanentId(data.getSpousePermanentId());

					if (spouse != null)
					{
						MarriageHandler.endMarriage(player, spouse);
						PlayerMemory memory = spouse.getPlayerMemory(player);

						spouse.getAI(AIMood.class).modifyMoodLevel(-5.0F);
						memory.setHearts(-100);
					}

					MarriageHandler.forceEndMarriage(player);
				}

				else
				{
					villager.say("interaction.divorce.priest.fail.notmarried", player);
				}
			}

			else if (interaction == EnumInteraction.RESETBABY)
			{
				PlayerData data = MCA.getPlayerData(player);

				if (data.getShouldHaveBaby())
				{
					villager.say("interaction.resetbaby.success", player);
					data.setShouldHaveBaby(false);

					for (int i = 0; i < player.inventory.mainInventory.length; i++)
					{
						ItemStack stack = player.inventory.getStackInSlot(i);

						if (stack != null && stack.getItem() instanceof ItemBaby)
						{
							ItemBaby baby = (ItemBaby) stack.getItem();

							if (stack.getTagCompound().getString("owner").equals(player.getName()))
							{
								player.inventory.setInventorySlotContents(i, null);
							}
						}
					}
				}

				else
				{
					villager.say("interaction.resetbaby.fail", player);
				}
			}

			else if (interaction == EnumInteraction.ADOPTBABY)
			{
				PlayerData data = MCA.getPlayerData(player);

				if (getIsOverChildrenCount(player))
				{
					player.addChatMessage(new ChatComponentText(Color.RED + "You have too many children."));
				}

				else if (!data.getShouldHaveBaby())
				{
					boolean isMale = RadixLogic.getBooleanWithProbability(50);
					String babyName = isMale ? MCA.getLanguageManager().getString("name.male") : MCA.getLanguageManager().getString("name.female");
					villager.say("interaction.adoptbaby.success", player, babyName);

					ItemStack stack = new ItemStack(isMale ? ModItems.babyBoy : ModItems.babyGirl);

					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("name", babyName);
					nbt.setInteger("age", 0);
					nbt.setString("owner", player.getName());
					stack.setTagCompound(nbt);
					
					player.inventory.addItemStackToInventory(stack);
					data.setShouldHaveBaby(true);
				}

				else
				{
					villager.say("interactionp.havebaby.fail.alreadyexists", player);
				}
			}

			else if (interaction == EnumInteraction.ACCEPT)
			{
				Integer length = (Integer) packet.additionalData[0];
				Boolean isExtending = (Boolean) packet.additionalData[1];
				PlayerMemory memory = villager.getPlayerMemory(player);

				if (isExtending)
				{
					memory.setIsHiredBy(true, memory.getHireTimeLeft() + (length * 60));
				}
				
				else
				{
					memory.setIsHiredBy(true, length * 60);
				}

				for (int i = 0; i < length; i++)
				{
					player.inventory.consumeInventoryItem(Items.gold_ingot);
				}
			}

			else if (interaction == EnumInteraction.PROCREATE)
			{
				PlayerData playerData = MCA.getPlayerData(player);

				if (getIsOverChildrenCount(player))
				{
					player.addChatMessage(new ChatComponentText(Color.RED + "You have too many children."));
				}

				else if (playerData.getShouldHaveBaby())
				{
					player.addChatMessage(new ChatComponentText(Color.RED + "You already have a baby."));
				}

				else
				{
					villager.getAI(AIProcreate.class).setIsProcreating(true);
				}
			}

			else if (interaction == EnumInteraction.DISMISS)
			{
				PlayerMemory memory = villager.getPlayerMemory(player);
				memory.setIsHiredBy(false, 0);
			}
		}
	}

	private boolean getIsOverChildrenCount(EntityPlayer player)
	{
		PlayerData playerData = MCA.getPlayerData(player);
		int childrenCount = 0;

		for (Object obj : MinecraftServer.getServer().worldServers[0].loadedEntityList)
		{
			if (obj instanceof EntityHuman)
			{
				EntityHuman human = (EntityHuman)obj;

				if (human.isPlayerAParent(player))
				{
					childrenCount++;
				}
			}
		}

		return childrenCount >= MCA.getConfig().childLimit && MCA.getConfig().childLimit != -1;
	}
}

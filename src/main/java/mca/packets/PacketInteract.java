package mca.packets;

import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
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
import mca.util.TutorialMessage;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import radixcore.constant.Font.Color;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
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
						block = villager.worldObj.getBlock(iPosX, iPosY, iPosZ);
					}

					else if (!Utilities.isPointClear(villager.worldObj, iPosX, iPosY + 1, iPosZ))
					{
						block = villager.worldObj.getBlock(iPosX, iPosY + 1, iPosZ);
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
				player.displayGUIMerchant(villager, villager.getTitle(player));
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
							if (stack.stackTagCompound.getString("owner").equals(player.getCommandSenderName()))
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
					stack.stackTagCompound = new NBTTagCompound();
					stack.stackTagCompound.setString("name", babyName);
					stack.stackTagCompound.setInteger("age", 0);
					stack.stackTagCompound.setString("owner", player.getCommandSenderName());

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
			
			else if (interaction == EnumInteraction.TAXES)
			{
				List<Entity> villagerList = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, villager, 50);
				int percentAverage = getVillageHappinessPercentage(villager, player, villagerList);
				
				if (percentAverage != -1)
				{
					PlayerMemory thisMemory = villager.getPlayerMemory(player);
					Item dropItem = RadixLogic.getBooleanWithProbability(3) ? Items.diamond : 
						RadixLogic.getBooleanWithProbability(50) ? Items.gold_nugget : Items.iron_ingot;
					int	happinessLevel = MathHelper.clamp_int((int)Math.round(percentAverage / 25), 0, 4);
					int	itemsDropped = RadixMath.getNumberInRange(Math.round((float)happinessLevel / 2), happinessLevel * 2);
					
					if (itemsDropped == 0) //On happiness level 0, make sure just one is dropped.
					{
						itemsDropped++;
					}
						
					if (dropItem == Items.diamond) //Halve what will be received from a rare diamond drop.
					{
						itemsDropped = MathHelper.clamp_int(itemsDropped, 1, 5);
					}
					
					if (happinessLevel <= 2)
					{
						MCA.getPacketHandler().sendPacketToPlayer(new PacketSetTutorialMessage(
							new TutorialMessage("Unhappy villagers do not like being taxed, and will contribute less.", 
									"Increase their happiness by maintaining high hearts with them.")), (EntityPlayerMP) player);
					}
						
					//Randomly decrease hearts of all villagers around.
					for (Entity entity : villagerList)
					{
						if (RadixLogic.getBooleanWithProbability(50))
						{
							EntityHuman human = (EntityHuman)entity;
							PlayerMemory memory = human.getPlayerMemory(player);
							memory.setHearts(memory.getHearts() - RadixMath.getNumberInRange(3, 8));
						}
					}
						
					//Drop the item.
					villager.entityDropItem(new ItemStack(dropItem, itemsDropped), 1.0F);
						
					//Comment on village happiness and reset this villager's tax time.
					villager.say("interaction.tax.happylevel" + happinessLevel, player);
					thisMemory.setTaxResetCounter(20); //Set to 20 minutes.
				}
				
				else
				{
					villager.say("interaction.tax.notlargeenough", player);
				}
			}

			else if (interaction == EnumInteraction.CHECKHAPPINESS)
			{
				List<Entity> villagerList = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, villager, 50);
				int happinessPercent = getVillageHappinessPercentage(villager, player, villagerList);
				int requiredVillagers = 10 - villagerList.size();
				
				if (happinessPercent == -1)
				{
					villager.say("interaction.checkhappiness.fail", player, requiredVillagers);					
				}
				
				else
				{
					villager.say("interaction.checkhappiness.success", player, happinessPercent);
				}
			}
		}

		return null;
	}

	private boolean getIsOverChildrenCount(EntityPlayer player)
	{
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
	
	private int getVillageHappinessPercentage(EntityHuman villager, EntityPlayer player, List<Entity> villagerList)
	{
		int villagersInArea = villagerList.size();
				
		if (villagersInArea >= 10)
		{
			//Calculate total hearts and averages.
			int totalHearts = 0;
			int percentAverage = 0;
			double averageHearts = 0;
			
			for (Entity entity : villagerList)
			{
				EntityHuman human = (EntityHuman)entity;
				PlayerMemory memory = human.getPlayerMemory(player);
				totalHearts += MathHelper.clamp_int(memory.getHearts(), -100, 100);
			}
			
			averageHearts = (float)totalHearts / (float)(villagersInArea * 100);
			percentAverage = (int) (averageHearts * 100);
			return percentAverage;
		}
		
		else
		{
			return -1;
		}
	}
}

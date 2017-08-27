package mca.packets;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.actions.ActionFollow;
import mca.actions.ActionProcreate;
import mca.actions.ActionSleep;
import mca.actions.ActionUpdateMood;
import mca.api.RegistryMCA;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;
import mca.enums.EnumInteraction;
import mca.enums.EnumMovementState;
import mca.enums.EnumPersonality;
import mca.items.ItemBaby;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.constant.Font.Color;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketInteract extends AbstractPacket<PacketInteract>
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
			additionalData[i] = RadixNettyIO.readObject(byteBuf);
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
				RadixNettyIO.writeObject(byteBuf, obj);
			}
		}
	}

	@Override
	public void processOnGameThread(PacketInteract message, MessageContext context) 
	{
		PacketInteract packet = (PacketInteract)message;
		EntityVillagerMCA villager = null;
		EntityPlayer player = null;

		for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
		{
			player = getPlayer(context);
			villager = (EntityVillagerMCA) world.getEntityByID(packet.entityId);

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
				if (villager.getBehavior(ActionSleep.class).setHomePoint(villager.posX, villager.posY, villager.posZ))
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
					BlockPos pos = new BlockPos(iPosX, iPosY, iPosZ);
					
					if (!Utilities.isPointClear(villager.world, iPosX, iPosY, iPosZ))
					{
						block = villager.world.getBlockState(pos).getBlock();
					}

					else if (!Utilities.isPointClear(villager.world, iPosX, iPosY + 1, iPosZ))
					{
						block = villager.world.getBlockState(pos.add(0, 1, 0)).getBlock();
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
				villager.startRiding(player);
			}

			else if (interaction == EnumInteraction.TAKE_GIFT)
			{
				PlayerMemory memory = villager.attributes.getPlayerMemory(player);
				memory.setHasGift(false);

				ItemStack stack = RegistryMCA.getGiftStackFromRelationship(memory.getHearts());
				villager.dropItem(stack.getItem(), stack.getCount());
			}

			else if (interaction == EnumInteraction.CHAT || interaction == EnumInteraction.JOKE || interaction == EnumInteraction.SHAKE_HAND ||
					interaction == EnumInteraction.TELL_STORY || interaction == EnumInteraction.FLIRT || interaction == EnumInteraction.HUG ||
					interaction == EnumInteraction.KISS)
			{
				ActionUpdateMood mood = villager.getBehavior(ActionUpdateMood.class);
				PlayerMemory memory = villager.attributes.getPlayerMemory(player);

				//First check for spouse leaving due to low hearts.
				if (memory.getDialogueType() == EnumDialogueType.SPOUSE && memory.getHearts() <= 25 && villager.attributes.getLowHeartWarnings() >= 3)
				{
					villager.say("spouse.endmarriage", player, player);
					player.sendMessage(new TextComponentString(Color.RED + MCA.getLocalizer().getString("notify.spouseendedmarriage", villager)));
					memory.setHearts(-100);
					mood.modifyMoodLevel(-20.0F);
					villager.attributes.resetLowHeartWarnings();

					NBTPlayerData playerData = MCA.getPlayerData(player);
					playerData.setSpouse(null);
					villager.endMarriage();
				}

				else
				{
					int successChance = interaction.getSuccessChance(villager, memory);

					int pointsModification = interaction.getBasePoints()
							+ villager.attributes.getPersonality().getHeartsModifierForInteraction(interaction) 
							+ mood.getMood(villager.attributes.getPersonality()).getPointsModifierForInteraction(interaction);

					boolean wasGood = RadixLogic.getBooleanWithProbability(successChance);

					if (villager.attributes.getPersonality() == EnumPersonality.FRIENDLY)
					{
						pointsModification += pointsModification * 0.15D;
					}

					else if (villager.attributes.getPersonality() == EnumPersonality.FLIRTY)
					{
						pointsModification += pointsModification * 0.25D;
					}

					else if (villager.attributes.getPersonality() == EnumPersonality.SENSITIVE && RadixLogic.getBooleanWithProbability(5))
					{
						pointsModification = -35;
						wasGood = false;
					}

					else if (villager.attributes.getPersonality() == EnumPersonality.STUBBORN)
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
						//player.addStat(AchievementsMCA.fullGoldHearts);
					}

					if (memory.getInteractionFatigue() == 4)
					{
						TutorialManager.sendMessageToPlayer(player, "Villagers tire of conversation after a few tries.", "Talk to them later for better success chances.");
					}
				}
			}

			else if (interaction == EnumInteraction.FOLLOW)
			{
				villager.attributes.setMovementState(EnumMovementState.FOLLOW);
				villager.getBehavior(ActionFollow.class).setFollowingUUID(player.getUniqueID());
			}
			
			else if (interaction == EnumInteraction.STAY)
			{
				villager.attributes.setMovementState(EnumMovementState.STAY);
			}
			
			else if (interaction == EnumInteraction.MOVE)
			{
				villager.attributes.setMovementState(EnumMovementState.MOVE);
			}
			
			else if (interaction == EnumInteraction.STOP)
			{
				villager.getBehaviors().disableAllToggleActions();
			}

			else if (interaction == EnumInteraction.INVENTORY)
			{
				villager.attributes.setDoOpenInventory(true);
			}

			else if (interaction == EnumInteraction.RIDE_HORSE)
			{
				if (villager.getRidingEntity() != null)
				{
					//horseSaddled is set to false when mounted by a villager in order for
					//the navigator to function properly and make them move. Set them back
					//as saddled when the villager dismounts.
					EntityHorse horse = (EntityHorse)villager.getRidingEntity();
					horse.setHorseSaddled(true);

					villager.dismountRidingEntity();
				}

				else
				{
					EntityHorse horse = (EntityHorse)RadixLogic.getClosestEntityExclusive(villager, 5, EntityHorse.class);

					if (horse != null)
					{
						if (horse.isHorseSaddled() && !horse.isBeingRidden())
						{
							villager.startRiding(horse);
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
				NBTPlayerData data = MCA.getPlayerData(player);

				if (data.getSpouseUUID() != Constants.EMPTY_UUID)
				{
					villager.say("interaction.divorce.priest.success", player);

					EntityVillagerMCA spouse = (EntityVillagerMCA) MCA.getEntityByUUID(villager.world, data.getSpouseUUID());

					if (spouse != null)
					{
						spouse.endMarriage();
						PlayerMemory memory = spouse.attributes.getPlayerMemory(player);

						spouse.getBehavior(ActionUpdateMood.class).modifyMoodLevel(-5.0F);
						memory.setHearts(-100);
					}

					data.setSpouse(null);
				}

				else
				{
					villager.say("interaction.divorce.priest.fail.notmarried", player);
				}
			}

			else if (interaction == EnumInteraction.RESETBABY)
			{
				NBTPlayerData data = MCA.getPlayerData(player);

				if (data.getOwnsBaby())
				{
					villager.say("interaction.resetbaby.success", player);
					data.setOwnsBaby(false);

					for (int i = 0; i < player.inventory.mainInventory.size(); i++)
					{
						ItemStack stack = player.inventory.getStackInSlot(i);

						if (stack != null && stack.getItem() instanceof ItemBaby)
						{
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
				NBTPlayerData data = MCA.getPlayerData(player);

				if (getIsOverChildrenCount(player))
				{
					player.sendMessage(new TextComponentString(Color.RED + "You have too many children."));
				}

				else if (!data.getOwnsBaby())
				{
					boolean isMale = RadixLogic.getBooleanWithProbability(50);
					String babyName = isMale ? MCA.getLocalizer().getString("name.male") : MCA.getLocalizer().getString("name.female");
					villager.say("interaction.adoptbaby.success", player, babyName);

					ItemStack stack = new ItemStack(isMale ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);

					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("name", babyName);
					nbt.setInteger("age", 0);
					nbt.setString("owner", player.getName());
					stack.setTagCompound(nbt);
					
					player.inventory.addItemStackToInventory(stack);
					data.setOwnsBaby(true);
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
				PlayerMemory memory = villager.attributes.getPlayerMemory(player);

				if (isExtending)
				{
					memory.setIsHiredBy(true, memory.getHireTimeLeft() + (length * 60));
				}
				
				else
				{
					memory.setIsHiredBy(true, length * 60);
				}

				int currentSlot = 0;
				for (int i = 0; i < 3; i++)
				{
					int slot = -1;
					for (; currentSlot < player.inventory.mainInventory.size(); currentSlot++) {
						ItemStack stack = player.inventory.mainInventory.get(currentSlot);
						if (stack != null && stack.getItem() == Items.GOLD_INGOT) {
							slot = currentSlot;
							break;
						}
					}

					if (slot > -1)
					{
						player.inventory.decrStackSize(slot, 1);
					}
				}
			}

			else if (interaction == EnumInteraction.PROCREATE)
			{
				NBTPlayerData playerData = MCA.getPlayerData(player);

				if (getIsOverChildrenCount(player))
				{
					player.sendMessage(new TextComponentString(Color.RED + "You have too many children."));
				}

				else if (playerData.getOwnsBaby())
				{
					player.sendMessage(new TextComponentString(Color.RED + "You already have a baby."));
				}

				else
				{
					villager.getBehavior(ActionProcreate.class).setIsProcreating(true);
				}
			}

			else if (interaction == EnumInteraction.DISMISS)
			{
				PlayerMemory memory = villager.attributes.getPlayerMemory(player);
				memory.setIsHiredBy(false, 0);
			}
			
			else if (interaction == EnumInteraction.TAXES)
			{
				List<EntityVillagerMCA> villagerList = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, villager, 50);
				int percentAverage = getVillageHappinessPercentage(villager, player, villagerList);
				
				if (percentAverage != -1)
				{
					PlayerMemory thisMemory = villager.attributes.getPlayerMemory(player);
					Item dropItem = RadixLogic.getBooleanWithProbability(3) ? Items.DIAMOND : 
						RadixLogic.getBooleanWithProbability(50) ? Items.GOLD_NUGGET : Items.IRON_INGOT;
					int	happinessLevel = MathHelper.clamp((int)Math.round(percentAverage / 25), 0, 4);
					int	itemsDropped = RadixMath.getNumberInRange(Math.round((float)happinessLevel / 2), happinessLevel * 2);
					
					if (itemsDropped == 0) //On happiness level 0, make sure just one is dropped.
					{
						itemsDropped++;
					}
						
					if (dropItem == Items.DIAMOND) //Halve what will be received from a rare diamond drop.
					{
						itemsDropped = MathHelper.clamp(itemsDropped, 1, 5);
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
							EntityVillagerMCA human = (EntityVillagerMCA)entity;
							PlayerMemory memory = human.attributes.getPlayerMemory(player);
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
				NBTPlayerData data = MCA.getPlayerData(player);

				List<EntityVillagerMCA> villagerList = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, villager, 50);
				int percentAverage = getVillageHappinessPercentage(villager, player, villagerList);
				
				int happinessPercent = getVillageHappinessPercentage(villager, player, villagerList);
				int requiredVillagers = 10 - villagerList.size();
				boolean flag = false; 
				
				if (happinessPercent == -1)
				{
					villager.say("interaction.checkhappiness.fail", player, requiredVillagers);
				}
				
				else
				{
					villager.say("interaction.checkhappiness.success", player, happinessPercent);
					
					if (happinessPercent > 80)
					{
						TutorialManager.sendMessageToPlayer(player, "Once your village's happiness is at 80% or more they may", "soon ask you to become a baron or baroness.");
						flag = true;
					}
				}
				
				//Setting the flag again on a noble can cause the greeting for becoming baron to come up again.
				if (!data.getIsNobility())
				{
					data.setHappinessThresholdMet(flag);
				}
			}
			
			else if (interaction == EnumInteraction.NOBILITY_PROMPT_ACCEPT)
			{
				NBTPlayerData data = MCA.getPlayerData(player);
				data.setIsNobility(true);
				data.setHappinessThresholdMet(false);
			}
		}
	}

	private boolean getIsOverChildrenCount(EntityPlayer player)
	{
		int childrenCount = 0;
		
		for (Object obj : FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0].loadedEntityList)
		{
			if (obj instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA human = (EntityVillagerMCA)obj;

				if (human.attributes.isPlayerAParent(player))
				{
					childrenCount++;
				}
			}
		}

		return childrenCount >= MCA.getConfig().childLimit && MCA.getConfig().childLimit != -1;
	}
	
	private int getVillageHappinessPercentage(EntityVillagerMCA villager, EntityPlayer player, List<EntityVillagerMCA> villagerList)
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
				EntityVillagerMCA human = (EntityVillagerMCA)entity;
				PlayerMemory memory = human.attributes.getPlayerMemory(player);
				totalHearts += MathHelper.clamp(memory.getHearts(), -100, 100);
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
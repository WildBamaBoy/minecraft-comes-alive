package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.ai.AIGrow;
import mca.ai.AIMood;
import mca.ai.AIProgressStory;
import mca.api.IGiftableItem;
import mca.api.RegistryMCA;
import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumBabyState;
import mca.enums.EnumProgressionStep;
import mca.util.MarriageHandler;
import mca.util.TutorialManager;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.inventory.Inventory;
import radixcore.packets.AbstractPacket;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class PacketGift extends AbstractPacket implements IMessage, IMessageHandler<PacketGift, IMessage>
{
	private int entityId;
	private int slot;

	public PacketGift()
	{
	}

	public PacketGift(EntityHuman human, int slot)
	{
		this.entityId = human.getEntityId();
		this.slot = slot;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		entityId = byteBuf.readInt();
		slot = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(entityId);
		byteBuf.writeInt(slot);
	}

	@Override
	public IMessage onMessage(PacketGift packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	private boolean handleWeddingRing(EntityPlayer player, EntityHuman human)
	{
		NBTPlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		if (human.getIsChild() || !human.allowIntimateInteractions(player))
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (data.getSpousePermanentId() == human.getPermanentId() && (!human.getIsEngaged() || human.getIsMarried()))
		{
			human.say("interaction.marry.fail.marriedtogiver", player); 
		}

		else if (human.getIsMarried())
		{
			human.say("interaction.marry.fail.marriedtoother", player); 
		}

		else if (human.getIsEngaged() && human.getSpouseId() != data.getPermanentId())
		{
			human.say("interaction.engage.fail.engagedtoother", player); 
		}

		else if (data.getSpousePermanentId() != 0 && !data.getIsEngaged())
		{
			human.say("interaction.marry.fail.playermarried", player); 
		}

		else if (memory.getHearts() < 95)
		{
			human.say("interaction.marry.fail.lowhearts", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have 5 golden hearts with", "a villager before marrying them.");
		}

		else
		{
			player.addStat(ModAchievements.marriage);
			human.say("interaction.marry.success", player); 

			MarriageHandler.startMarriage(player, human);
			memory.setIsHiredBy(false, 0);
			
			human.getAI(AIMood.class).modifyMoodLevel(3.0F);
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now married. You can have", "children by using the 'Procreate' button.");
			return true;
		}

		return false;
	}

	private boolean handleMatchmakersRing(EntityPlayer player, EntityHuman human, ItemStack stack)
	{
		EntityHuman partner = (EntityHuman) RadixLogic.getNearestEntityOfTypeWithinDistance(EntityHuman.class, human, 5);

		if (human.getIsChild())
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (human.getIsMarried())
		{
			human.say("interaction.matchmaker.fail.married", player); 
		}

		else if (human.getIsEngaged())
		{
			human.say("interaction.matchmaker.fail.engaged", player); 
		}

		else if (stack.stackSize < 2)
		{
			human.say("interaction.matchmaker.fail.needtwo", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have two matchmaker's rings", "in a stack to arrange a marriage.");
		}

		else
		{	
			boolean partnerIsValid = partner != null 
					&& !partner.getIsMarried() 
					&& !partner.getIsEngaged() 
					&& !partner.getIsChild() 
					&& (partner.getFatherId() == -1 || partner.getFatherId() != human.getFatherId()) 
					&& (partner.getMotherId() == -1 || partner.getMotherId() != human.getMotherId());

			if (partner == null)
			{
				human.say("interaction.matchmaker.fail.novillagers", player); 
				TutorialManager.sendMessageToPlayer(player, "To arrange a marriage, have two rings in a stack and ", "make sure another marriable villager is nearby.");
			}

			else if (!partnerIsValid)
			{
				human.say("interaction.matchmaker.fail.invalid", player, partner); 
				TutorialManager.sendMessageToPlayer(player, "A married villager, relative, or child was too close to this", "villager. Move this villager away from anyone not marriable.");
			}

			else
			{
				human.setMarriedTo(partner);
				partner.setMarriedTo(human);

				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, partner, 16);

				for (Object obj : human.worldObj.playerEntities)
				{
					EntityPlayer onlinePlayer = (EntityPlayer)obj;

					if (human.isPlayerAParent(onlinePlayer) || partner.isPlayerAParent(onlinePlayer))
					{
						onlinePlayer.addStat(ModAchievements.childMarried);	
					}
				}

				TutorialManager.sendMessageToPlayer(player, "These villagers are now married.", "They will have children in the near future.");
				return true;
			}
		}

		return false;
	}

	private boolean handleEngagementRing(EntityPlayer player, EntityHuman human)
	{
		NBTPlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		if (human.getIsChild() || !human.allowIntimateInteractions(player))
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (data.getSpousePermanentId() == human.getPermanentId())
		{
			human.say("interaction.marry.fail.marriedtogiver", player); 
		}

		else if (human.getIsMarried())
		{
			human.say("interaction.marry.fail.marriedtoother", player); 
		}

		else if (human.getIsEngaged())
		{
			human.say("interaction.engage.fail.engagedtoother", player); 
		}

		else if (data.getSpousePermanentId() != 0)
		{
			human.say("interaction.marry.fail.playermarried", player); 
		}

		else if (memory.getHearts() < 95)
		{
			human.say("interaction.marry.fail.lowhearts", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have 5 golden hearts with", "a villager before proposing.");
		}

		else
		{
			player.addStat(ModAchievements.engagement);
			human.say("interaction.engage.success", player); 

			MarriageHandler.startEngagement(player, human);
			memory.setIsHiredBy(false, 0);
			
			human.getAI(AIMood.class).modifyMoodLevel(3.0F);
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now engaged. Now gift a wedding ring", "to get gifts from other villagers.");
			return true;
		}

		return false;
	}

	private boolean handleDivorcePapers(EntityPlayer player, EntityHuman human) 
	{
		NBTPlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		if (human.getIsChild())
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (!human.getIsMarried())
		{
			human.say("interaction.divorce.notmarried", player); 
		}

		else if (human.isMarriedToAPlayer() && data.getSpousePermanentId() != human.getPermanentId())
		{
			human.say("interaction.divorce.notmarriedtoplayer", player); 
		}

		else
		{
			if (human.isMarriedToAPlayer())
			{
				memory.setHearts(-100);
				human.say("interaction.divorce.success", player); 

				MarriageHandler.endMarriage(player, human);

				human.getAI(AIMood.class).modifyMoodLevel(-10.0F);
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_ANGRY, human, 16);
			}

			else
			{
				final EntityHuman partner = human.getVillagerSpouse();

				if (partner != null)
				{
					partner.setMarriedTo(null);
				}

				human.setMarriedTo(null);
			}

			return true;
		}

		return false;
	}

	private boolean handleStandardGift(EntityPlayer player, EntityHuman human, int slot, ItemStack stack)
	{
		final PlayerMemory memory = human.getPlayerMemory(player);
		final Object queryObject = stack.getItem() instanceof ItemBlock ? Block.getBlockFromItem(stack.getItem()) : stack.getItem();
		int giftValue = RegistryMCA.getGiftMap().containsKey(queryObject) ? RegistryMCA.getGiftMap().get(queryObject) : -5;

		if (giftValue == -5 && stack.getItem() instanceof IGiftableItem) //Not contained in gift map.
		{
			IGiftableItem item = (IGiftableItem)stack.getItem();
			giftValue = item.getGiftValue();
		}

		final int heartsModify = RadixMath.clamp(giftValue - (memory.getInteractionFatigue() * 4), -5, Integer.MAX_VALUE);
		final String giftType = heartsModify <= 0 ? "bad" : heartsModify <= 5 ? "good" : heartsModify <= 10 ? "better" : "best";

		memory.setHearts(memory.getHearts() + heartsModify);
		memory.increaseInteractionFatigue();

		human.say(memory.getDialogueType().toString() + ".gift." + giftType, player);

		if (giftValue > 0 && heartsModify <= 0)
		{
			TutorialManager.sendMessageToPlayer(player, "You have interacted with this villager too much.", "Wait before interacting again.");
		}

		else if (giftValue <= 0)
		{
			TutorialManager.sendMessageToPlayer(player, "This villager did not like your gift.", "Try a different item next time.");
		}

		if (heartsModify > 0)
		{
			human.getAI(AIMood.class).modifyMoodLevel(1.0F);
			return true;
		}

		else
		{
			human.getAI(AIMood.class).modifyMoodLevel(-1.0F);
		}

		return false;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		final PacketGift packet = (PacketGift)message;
		
		EntityHuman human = null;
		EntityPlayer player = null;

		for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers)
		{
			player = getPlayer(context);
			human = (EntityHuman) world.getEntityByID(packet.entityId);

			if (player != null && human != null)
			{
				break;
			}
		}

		if (player != null && human != null)
		{
			final ItemStack stack = player.inventory.mainInventory[packet.slot];
			final Item item = stack.getItem();
			boolean removeItem = false;
			int removeCount = 1;

			if (item == ModItems.weddingRing || item == ModItems.weddingRingRG)
			{
				removeItem = handleWeddingRing(player, human);
			}

			else if (item == ModItems.matchmakersRing)
			{
				removeItem = handleMatchmakersRing(player, human, stack);
				removeCount = 2;
			}

			else if (item == ModItems.engagementRing || item == ModItems.weddingRingRG || item == ModItems.engagementRingRG 
					|| item == ModItems.engagementRingHeart || item == ModItems.engagementRingOval 
					|| item == ModItems.engagementRingSquare || item == ModItems.engagementRingStar || item == ModItems.engagementRingTiny || item == ModItems.engagementRingTriangle
					|| item == ModItems.engagementRingHeartRG || item == ModItems.engagementRingOvalRG || item == ModItems.engagementRingSquareRG
					|| item == ModItems.engagementRingStarRG || item == ModItems.engagementRingTinyRG || item == ModItems.engagementRingTriangleRG
					)
			{
				removeItem = handleEngagementRing(player, human);
			}

			else if (item == ModItems.divorcePapers)
			{
				removeItem = handleDivorcePapers(player, human);
			}

			else if (human.getIsInfected() && human.getActivePotionEffect(MobEffects.weakness) != null && stack.getItem() == Items.golden_apple)
			{
				removeItem = true;
				removeCount = 1;
				
				human.cureInfection();
			}
			
			else if (item == Items.golden_apple && human.getIsChild() && human.isPlayerAParent(player))
			{
				removeItem = true;
				removeCount = 1;

				human.getAI(AIGrow.class).accelerate();
			}

			else if ((item == ModItems.babyBoy || item == ModItems.babyGirl) && human.getPlayerSpouse() == player)
			{
				removeItem = true;
				removeCount = 1;

				human.getVillagerInventory().addItemStackToInventory(stack);
			}

			else if (item == Items.cake || Block.getBlockFromItem(item) == Blocks.cake)
			{
				EnumProgressionStep step = human.getAI(AIProgressStory.class).getProgressionStep();

				if (human.isMarriedToAVillager() && human.getVillagerSpouse() != null && RadixMath.getDistanceToEntity(human, human.getVillagerSpouse()) <= 8.5D)
				{
					EntityHuman spouse = human.getVillagerSpouse();

					removeItem = true;
					removeCount = 1;

					if (human.getBabyState() == EnumBabyState.NONE && spouse.getBabyState() == EnumBabyState.NONE)
					{
						human.say("gift.cake" + RadixMath.getNumberInRange(1, 3), player);

						final EntityHuman progressor = !human.getIsMale() ? human : !spouse.getIsMale() ? spouse : human;
						human.getAI(AIProgressStory.class).setProgressionStep(EnumProgressionStep.HAD_BABY);
						spouse.getAI(AIProgressStory.class).setProgressionStep(EnumProgressionStep.HAD_BABY);

						Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
						Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, spouse, 16);

						progressor.setBabyState(EnumBabyState.getRandomGender());
					}

					else
					{
						human.sayRaw("We already have a baby.", player);
					}

					TutorialManager.sendMessageToPlayer(player, "Cake can influence villagers to have children.", "");
				}

				else if (human.isMarriedToAVillager() && human.getVillagerSpouse() == null)
				{
					human.sayRaw("I don't see my spouse anywhere...", player);
				}
				else
				{
					removeCount = 1;
					removeItem = handleStandardGift(player, human, packet.slot, stack);
				}
			}

			else if (item == ModItems.newOutfit && human.allowControllingInteractions(player))
			{
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, human, 16);
				human.setClothesTexture(human.getRandomSkin());
				removeItem = true;
				removeCount = 1;
			}

			else if (item instanceof ItemArmor)
			{
				removeItem = true;
				removeCount = 1;

				Inventory inventory = human.getVillagerInventory();
				ItemArmor armor = (ItemArmor)item;
				int inventorySlot = 0;

				switch (armor.armorType.getIndex())
				{
				case 3: inventorySlot = 36; break;
				case 2: inventorySlot = 37; break;
				case 1: inventorySlot = 38; break;
				case 0: inventorySlot = 39; break;
				}

				//Check for an existing armor item and drop it to make room for the new one.
				ItemStack stackInArmorSlot = inventory.getStackInSlot(inventorySlot);

				if (stackInArmorSlot != null)
				{
					human.entityDropItem(stackInArmorSlot, 1.0F);
				}

				//Add the new armor item to its respective slot.
			}

			else
			{
				removeItem = handleStandardGift(player, human, packet.slot, stack);
			}

			if (removeItem && !player.capabilities.isCreativeMode)
			{
				stack.stackSize -= removeCount;

				if (stack.stackSize > 0)
				{
					player.inventory.setInventorySlotContents(packet.slot, stack);
				}

				else
				{
					player.inventory.setInventorySlotContents(packet.slot, null);				
				}
			}
		}
	}
}

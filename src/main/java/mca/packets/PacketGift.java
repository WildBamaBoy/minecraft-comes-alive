package mca.packets;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.actions.ActionGrow;
import mca.actions.ActionStoryProgression;
import mca.actions.ActionUpdateMood;
import mca.api.IGiftableItem;
import mca.api.RegistryMCA;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumMarriageState;
import mca.enums.EnumProfession;
import mca.enums.EnumProgressionStep;
import mca.inventory.VillagerInventory;
import mca.util.Either;
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
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.net.AbstractPacket;

public class PacketGift extends AbstractPacket<PacketGift>
{
	private int entityId;
	private int slot;

	public PacketGift()
	{
	}

	public PacketGift(EntityVillagerMCA human, int slot)
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

	private boolean handleWeddingRing(EntityPlayer player, EntityVillagerMCA human)
	{
		NBTPlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.attributes.getPlayerMemory(player);

		if (human.attributes.getIsChild() || !human.attributes.allowsIntimateInteractions(player))
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (data.getSpouseUUID() == human.getPersistentID() && human.attributes.getMarriageState() == EnumMarriageState.ENGAGED)
		{
			//Violates DRY, yes, but it will work for now

			human.say("interaction.marry.success", player); 

			human.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withR(player));
			memory.setIsHiredBy(false, 0);
			
			human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(3.0F);
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now married. You can have", "children by using the 'Procreate' button.");
			
			List<EntityVillagerMCA> nearbyVillagers = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, player, 30);
			
			for (EntityVillagerMCA villager : nearbyVillagers)
			{
				PlayerMemory otherVillagerMemory = villager.attributes.getPlayerMemory(player);
				otherVillagerMemory.setHasGift(true);
			}
			
			return true;
		}
		
		else if (data.getSpouseUUID() == human.getPersistentID())
		{
			human.say("interaction.marry.fail.marriedtogiver", player); 
		}

		else if (human.attributes.getMarriageState() != EnumMarriageState.NOT_MARRIED)
		{
			human.say("interaction.marry.fail.marriedtoother", player); 
		}

		else if (human.attributes.getMarriageState() == EnumMarriageState.ENGAGED && human.attributes.getSpouseUUID() != data.getUUID())
		{
			human.say("interaction.engage.fail.engagedtoother", player); 
		}

		else if (data.getSpouseUUID() != Constants.EMPTY_UUID && data.getMarriageState() != EnumMarriageState.NOT_MARRIED)
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
			//player.addStat(AchievementsMCA.marriage);
			human.say("interaction.marry.success", player); 

			human.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withR(player));
			memory.setIsHiredBy(false, 0);
			
			human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(3.0F);
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now married. You can have", "children by using the 'Procreate' button.");
			return true;
		}

		return false;
	}

	private boolean handleMatchmakersRing(EntityPlayer player, EntityVillagerMCA human, ItemStack stack)
	{
		EntityVillagerMCA partner = RadixLogic.getClosestEntity(Point3D.fromEntityPosition(human), human.world, 5, EntityVillagerMCA.class, human);

		if (human.attributes.getIsChild())
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (human.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER || human.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER)
		{
			human.say("interaction.matchmaker.fail.married", player); 
		}

		else if (human.attributes.getMarriageState() == EnumMarriageState.ENGAGED)
		{
			human.say("interaction.matchmaker.fail.engaged", player); 
		}

		else if (stack.getCount() < 2)
		{
			human.say("interaction.matchmaker.fail.needtwo", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have two matchmaker's rings", "in a stack to arrange a marriage.");
		}

		else
		{	
			boolean partnerIsValid = partner != null 
					&& partner.attributes.getMarriageState() == EnumMarriageState.NOT_MARRIED 
					&& !partner.attributes.getIsChild() 
					&& (partner.attributes.getFatherUUID() == Constants.EMPTY_UUID || partner.attributes.getFatherUUID() != human.attributes.getFatherUUID()) 
					&& (partner.attributes.getMotherUUID() == Constants.EMPTY_UUID || partner.attributes.getMotherUUID() != human.attributes.getMotherUUID());

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
				human.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(partner));

				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, partner, 16);

				for (Object obj : human.world.playerEntities)
				{
					EntityPlayer onlinePlayer = (EntityPlayer)obj;

					if (human.attributes.isPlayerAParent(onlinePlayer) || partner.attributes.isPlayerAParent(onlinePlayer))
					{
						//onlinePlayer.addStat(AchievementsMCA.childMarried);	
					}
				}

				TutorialManager.sendMessageToPlayer(player, "These villagers are now married.", "They will have children in the near future.");
				return true;
			}
		}

		return false;
	}

	private boolean handleEngagementRing(EntityPlayer player, EntityVillagerMCA human)
	{
		NBTPlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.attributes.getPlayerMemory(player);

		if (human.attributes.getIsChild() || !human.attributes.allowsIntimateInteractions(player))
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (data.getSpouseUUID() == human.getPersistentID())
		{
			human.say("interaction.marry.fail.marriedtogiver", player); 
		}

		else if (human.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER || human.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER)
		{
			human.say("interaction.marry.fail.marriedtoother", player); 
		}

		else if (human.attributes.getMarriageState() == EnumMarriageState.ENGAGED)
		{
			human.say("interaction.engage.fail.engagedtoother", player); 
		}

		else if (data.getSpouseUUID() != Constants.EMPTY_UUID)
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
			//player.addStat(AchievementsMCA.engagement);
			human.say("interaction.engage.success", player); 
			human.attributes.setFiancee(player);
			
			memory.setIsHiredBy(false, 0);
			
			human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(3.0F);
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now engaged. Now gift a wedding ring", "to get gifts from other villagers.");
			return true;
		}

		return false;
	}

	private boolean handleDivorcePapers(EntityPlayer player, EntityVillagerMCA human) 
	{
		NBTPlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.attributes.getPlayerMemory(player);

		if (human.attributes.getIsChild())
		{
			human.say("interaction.give.invalid", player); 
		}

		else if (human.attributes.getMarriageState() == EnumMarriageState.NOT_MARRIED)
		{
			human.say("interaction.divorce.notmarried", player); 
		}

		else if (human.attributes.isMarriedToAPlayer() && data.getSpouseUUID() != human.getPersistentID())
		{
			human.say("interaction.divorce.notmarriedtoplayer", player); 
		}

		else
		{
			if (human.attributes.isMarriedToAPlayer())
			{
				memory.setHearts(-100);
				human.say("interaction.divorce.success", player); 

				human.endMarriage();
				data.setSpouse(null);

				human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(-10.0F);
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_ANGRY, human, 16);
			}

			else
			{
				final EntityVillagerMCA partner = human.attributes.getVillagerSpouseInstance();

				if (partner != null)
				{
					partner.endMarriage();
				}

				human.endMarriage();
			}

			return true;
		}

		return false;
	}

	private boolean handleStandardGift(EntityPlayer player, EntityVillagerMCA human, int slot1, ItemStack stack)
	{
		final PlayerMemory memory = human.attributes.getPlayerMemory(player);
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
			human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(1.0F);
			return true;
		}

		else
		{
			human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(-1.0F);
		}

		return false;
	}

	@Override
	public void processOnGameThread(PacketGift packet, MessageContext context) 
	{
		EntityVillagerMCA human = null;
		EntityPlayer player = null;

		for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
		{
			player = getPlayer(context);
			human = (EntityVillagerMCA) world.getEntityByID(packet.entityId);

			if (player != null && human != null)
			{
				break;
			}
		}

		if (player != null && human != null)
		{
			final ItemStack stack = player.inventory.mainInventory.get(packet.slot);
			final Item item = stack.getItem();
			boolean removeItem = false;
			int removeCount = 1;

			if (item == ItemsMCA.WEDDING_RING || item == ItemsMCA.WEDDING_RING_RG)
			{
				removeItem = handleWeddingRing(player, human);
			}

			else if (item == ItemsMCA.MATCHMAKERS_RING)
			{
				removeItem = handleMatchmakersRing(player, human, stack);
				removeCount = 2;
			}

			else if (item == ItemsMCA.ENGAGEMENT_RING || item == ItemsMCA.ENGAGEMENT_RING_RG)
			{
				removeItem = handleEngagementRing(player, human);
			}

			else if (item == ItemsMCA.DIVORCE_PAPERS)
			{
				removeItem = handleDivorcePapers(player, human);
			}

			else if (item == ItemsMCA.BOOK_ROSE_GOLD && human.getName().equals("William") && human.attributes.getProfessionEnum() == EnumProfession.Miner)
			{
				removeItem = true;
				human.sayRaw("Oh, thank you for returning this to me! My company secrets could have been lost forever!", player);
			}
			
			else if (human.attributes.getIsInfected() && human.getActivePotionEffect(MobEffects.WEAKNESS) != null && stack.getItem() == Items.GOLDEN_APPLE)
			{
				removeItem = true;
				removeCount = 1;
				
				human.cureInfection();
			}
			
			else if (item == Items.GOLDEN_APPLE && human.attributes.getIsChild() && human.attributes.isPlayerAParent(player))
			{
				removeItem = true;
				removeCount = 1;

				human.getBehavior(ActionGrow.class).accelerate();
			}

			else if ((item == ItemsMCA.BABY_BOY || item == ItemsMCA.BABY_GIRL) && human.attributes.getPlayerSpouseInstance() == player)
			{
				removeItem = true;
				removeCount = 1;

				human.attributes.getInventory().addItem(stack);
			}

			else if (item == Items.CAKE || Block.getBlockFromItem(item) == Blocks.CAKE)
			{
				EnumProgressionStep step = human.getBehavior(ActionStoryProgression.class).getProgressionStep();

				if (human.attributes.isMarriedToAVillager() && human.attributes.getVillagerSpouseInstance() != null && RadixMath.getDistanceToEntity(human, human.attributes.getVillagerSpouseInstance()) <= 8.5D)
				{
					EntityVillagerMCA spouse = human.attributes.getVillagerSpouseInstance();

					removeItem = true;
					removeCount = 1;

					if (human.attributes.getBabyState() == EnumBabyState.NONE && spouse.attributes.getBabyState() == EnumBabyState.NONE)
					{
						human.say("gift.cake" + RadixMath.getNumberInRange(1, 3), player);

						final EntityVillagerMCA progressor = !human.attributes.getIsMale() ? human : !spouse.attributes.getIsMale() ? spouse : human;
						human.getBehavior(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.HAD_BABY);
						spouse.getBehavior(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.HAD_BABY);

						Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, human, 16);
						Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, spouse, 16);

						progressor.attributes.setBabyState(EnumBabyState.getRandomGender());
					}

					else
					{
						human.sayRaw("We already have a baby.", player);
					}

					TutorialManager.sendMessageToPlayer(player, "Cake can influence villagers to have children.", "");
				}

				else if (human.attributes.isMarriedToAVillager() && human.attributes.getVillagerSpouseInstance() == null)
				{
					human.sayRaw("I don't see my spouse anywhere...", player);
				}
				else
				{
					removeCount = 1;
					removeItem = handleStandardGift(player, human, packet.slot, stack);
				}
			}

			else if (item == ItemsMCA.NEW_OUTFIT && human.attributes.allowsControllingInteractions(player))
			{
				Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, human, 16);
				human.attributes.setClothesTexture(human.attributes.getProfessionSkinGroup().getRandomMaleSkin());
				removeItem = true;
				removeCount = 1;
			}

			else if (item instanceof ItemArmor)
			{
				removeItem = true;
				removeCount = 1;

				VillagerInventory inventory = human.attributes.getInventory();
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
				inventory.setInventorySlotContents(inventorySlot, new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
			}

			else
			{
				removeItem = handleStandardGift(player, human, packet.slot, stack);
			}

			if (removeItem && !player.capabilities.isCreativeMode)
			{
				stack.shrink(removeCount);

				if (stack.getCount() > 0)
				{
					player.inventory.setInventorySlotContents(packet.slot, stack);
				}

				else
				{
					player.inventory.setInventorySlotContents(packet.slot, ItemStack.EMPTY);				
				}
			}
		}
	}
}

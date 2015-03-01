package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.ai.AIMood;
import mca.api.ChoreRegistry;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.TutorialManager;
import mca.core.VersionBridge;
import mca.core.minecraft.Items;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumDialogueType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import radixcore.constant.Particle;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;
import radixcore.packets.AbstractPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

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
		EntityHuman human = null;
		EntityPlayer player = null;

		for (WorldServer world : MinecraftServer.getServer().worldServers)
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

			if (item == Items.weddingRing || item == Items.weddingRingRG)
			{
				removeItem = handleWeddingRing(player, human);
			}

			else if (item == Items.matchmakersRing)
			{
				removeItem = handleMatchmakersRing(player, human, stack);
				removeCount = 2;
			}

			else if (item == Items.engagementRing || item == Items.weddingRingRG || item == Items.coloredEngagementRingRG || item == Items.coloredEngagementRing)
			{
				removeItem = handleEngagementRing(player, human);
			}

			else if (item == Items.divorcePapers)
			{
				removeItem = handleDivorcePapers(player, human);
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

		return null;
	}

	private boolean handleWeddingRing(EntityPlayer player, EntityHuman human)
	{
		PlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		if (human.getIsChild() || !human.allowIntimateInteractions(player))
		{
			human.say("I can't use that.", player); 
		}
		
		else if (data.spousePermanentId.getInt() == human.getPermanentId() && (!human.getIsEngaged() || human.getIsMarried()))
		{
			human.say("I am married to you.", player); 
		}

		else if (human.getIsMarried())
		{
			human.say("I am already married.", player); 
		}

		else if (human.getIsEngaged() && human.getSpouseId() != data.permanentId.getInt())
		{
			human.say("I am engaged.", player); 
		}
		
		else if (data.spousePermanentId.getInt() != 0 && !data.isEngaged.getBoolean())
		{
			human.say("You are already married.", player); 
		}

		else if (memory.getHearts() < 95)
		{
			human.say("I don't like you.", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have 5 golden hearts with", "a villager before marrying them.");
		}

		else
		{
			human.say("We are now married!", player); 

			memory.setDialogueType(EnumDialogueType.SPOUSE);
			data.spousePermanentId.setValue(human.getPermanentId());
			human.setIsMarried(true, player);

			human.getAI(AIMood.class).modifyMoodLevel(3.0F);
			VersionBridge.spawnParticlesAroundEntityS(Particle.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now married. You can have", "children by using the 'Procreate' button.");
			return true;
		}
		
		return false;
	}

	private boolean handleMatchmakersRing(EntityPlayer player, EntityHuman human, ItemStack stack)
	{
		EntityHuman partner = (EntityHuman) LogicHelper.getNearestEntityOfTypeWithinDistance(EntityHuman.class, human, 5);
		
		if (human.getIsChild())
		{
			human.say("I can't use that.", player); 
		}
		
		else if (human.getIsMarried())
		{
			human.say("I am already married.", player); 
		}
		
		else if (human.getIsEngaged())
		{
			human.say("I am engaged.", player); 
		}
		
		else if (stack.stackSize < 2)
		{
			human.say("You need two of those.", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have two matchmaker's rings", "in a stack to arrange a marriage.");
		}
		
		else
		{	
			if (partner == null)
			{
				human.say("There was no-one nearby.", player); 
				TutorialManager.sendMessageToPlayer(player, "To arrange a marriage, have two rings in a stack and ", "make sure another marriable villager is nearby.");
			}
			
			else if (partner.getIsEngaged() || partner.getIsChild() || partner.getIsMarried()    || 
					(partner.getFatherId() != -1 && partner.getFatherId() == human.getFatherId()) || 
					(partner.getMotherId() != -1 && partner.getMotherId() == human.getMotherId()))
			{
				human.say("I can't marry " + partner.getName() + ".", player); 
				TutorialManager.sendMessageToPlayer(player, "A married villager, relative, or child was too close to this", "villager. Move this villager away from anyone not marriable.");
			}
			
			else
			{
				human.say("I will marry " + partner.getName() + ".", player); 
				human.setIsMarried(true, partner);
				partner.setIsMarried(true, human);
				
				VersionBridge.spawnParticlesAroundEntityS(Particle.HEART, human, 16);
				VersionBridge.spawnParticlesAroundEntityS(Particle.HEART, partner, 16);
				
				TutorialManager.sendMessageToPlayer(player, "These villagers are now married.", "They will have children in the near future.");
				return true;
			}
		}
		
		return false;
	}

	private boolean handleEngagementRing(EntityPlayer player, EntityHuman human)
	{
		PlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		if (human.getIsChild() || !human.allowIntimateInteractions(player))
		{
			human.say("I can't use that.", player); 
		}
		
		else if (data.spousePermanentId.getInt() == human.getPermanentId())
		{
			human.say("I am married to you.", player); 
		}

		else if (human.getIsMarried())
		{
			human.say("I am already married.", player); 
		}

		else if (human.getIsEngaged())
		{
			human.say("I am already engaged.", player); 
		}
		
		else if (data.spousePermanentId.getInt() != 0)
		{
			human.say("You are already married.", player); 
		}

		else if (memory.getHearts() < 95)
		{
			human.say("I don't like you.", player); 
			TutorialManager.sendMessageToPlayer(player, "You must have 5 golden hearts with", "a villager before proposing.");
		}

		else
		{
			human.say("We are now engaged!", player); 

			memory.setDialogueType(EnumDialogueType.SPOUSE);
			data.spousePermanentId.setValue(human.getPermanentId());
			data.isEngaged.setValue(true);
			human.setIsEngaged(true, player);

			human.getAI(AIMood.class).modifyMoodLevel(3.0F);
			VersionBridge.spawnParticlesAroundEntityS(Particle.HEART, human, 16);
			TutorialManager.sendMessageToPlayer(player, "You are now engaged. Now gift a wedding ring", "to get gifts from other villagers.");
			return true;
		}
		
		return false;
	}

	private boolean handleDivorcePapers(EntityPlayer player, EntityHuman human) 
	{
		PlayerData data = MCA.getPlayerData(player);
		PlayerMemory memory = human.getPlayerMemory(player);

		if (human.getIsChild())
		{
			human.say("I can't use that.", player); 
		}
		
		else if (!human.getIsMarried())
		{
			human.say("I am not married.", player); 
		}
		
		else if (human.isMarriedToAPlayer() && data.spousePermanentId.getInt() != human.getPermanentId())
		{
			human.say("I am not married to you.", player); 
		}

		else
		{
			if (human.isMarriedToAPlayer())
			{
				memory.setHearts(-100);
				
				human.say("We are no longer married.", player); 

				memory.setDialogueType(EnumDialogueType.ADULT);
				human.setIsMarried(false, (EntityHuman) null);
				human.setIsEngaged(false, (EntityPlayer) null);
				data.spousePermanentId.setValue(0);
				data.isEngaged.setValue(false);

				human.getAI(AIMood.class).modifyMoodLevel(-10.0F);
				VersionBridge.spawnParticlesAroundEntityS(Particle.ANGRY, human, 16);
			}
			
			else
			{
				human.say("I am no longer married to my spouse.", player); 
				
				final EntityHuman partner = human.getVillagerSpouse();
				
				if (partner != null)
				{
					partner.setIsMarried(false, (EntityHuman) null);
				}
				
				human.setIsMarried(false, (EntityHuman) null);
			}
			
			return true;
		}
		
		return false;
	}

	private boolean handleStandardGift(EntityPlayer player, EntityHuman human, int slot, ItemStack stack)
	{
		final PlayerMemory memory = human.getPlayerMemory(player);
		final Object queryObject = stack.getItem() instanceof ItemBlock ? Block.getBlockFromItem(stack.getItem()) : stack.getItem();
		final int giftValue = ChoreRegistry.getGiftMap().containsKey(queryObject) ? ChoreRegistry.getGiftMap().get(queryObject) : -5;

		final int heartsModify = MathHelper.clamp(giftValue - (memory.getInteractionFatigue() * 4), -5, Integer.MAX_VALUE);
		final String giftType = heartsModify <= 0 ? "bad" : heartsModify <= 5 ? "good" : heartsModify <= 10 ? "better" : "best";

		memory.setHearts(memory.getHearts() + heartsModify);
		memory.increaseInteractionFatigue();

		human.say(MCA.getLanguageManager().getString(memory.getDialogueType().toString() + ".gift." + giftType, human, player), player);

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
}

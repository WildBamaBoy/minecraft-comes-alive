/*******************************************************************************
 * EventHooks.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.io.File;

import mca.api.registries.VillagerRegistryMCA;
import mca.chore.ChoreCooking;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.WorldPropertiesList;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.AbstractSerializableEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.item.AbstractBaby;
import mca.item.ItemCrown;
import mca.item.ItemTombstone;
import mca.item.ItemWeddingRing;
import mca.network.packets.PacketOpenGui;
import mca.network.packets.PacketSayLocalized;
import mca.network.packets.PacketSetWorldProperties;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

import com.radixshock.radixcore.core.RadixCore;
import com.radixshock.radixcore.file.WorldPropertiesManager;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

/**
 * Contains methods that perform a function when an event in Minecraft occurs.
 */
public class EventHooks 
{
	/**
	 * Fired when the player throws an item away.
	 * 
	 * @param 	event	An instance of the ItemTossEvent.
	 */
	@SubscribeEvent
	public void itemTossedEventHandler(ItemTossEvent event)
	{
		final WorldPropertiesList properties = (WorldPropertiesList)MCA.getInstance().playerWorldManagerMap.get(event.player.getCommandSenderName()).worldPropertiesInstance;

		if (event.entityItem.getEntityItem().getItem() instanceof AbstractBaby && properties.babyExists)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(event.player, null, "notify.player.droppedbaby", false, null, null), (EntityPlayerMP)event.player);
			event.player.inventory.addItemStackToInventory(event.entityItem.getEntityItem());
			event.setCanceled(true);
			event.setResult(Result.DENY);
		}
	}

	/**
	 * Fired when an entity joins the world.
	 * 
	 * @param 	event	An instance of the EntityJoinWorldEvent.
	 */
	@SubscribeEvent
	public void entityJoinedWorldEventHandler(EntityJoinWorldEvent event)
	{
		if (!event.world.isRemote)
		{
			if (event.entity instanceof EntityMob)
			{
				doAddMobTasks((EntityMob)event.entity);
			}

			if (event.entity instanceof EntityVillager && !(event.entity instanceof AbstractSerializableEntity))
			{
				doOverwriteVillager(event, (EntityVillager)event.entity);
			}
		}
	}

	/**
	 * Fires when the world is saving.
	 * 
	 * @param 	event	An instance of the WorldEvent.Unload event.
	 */
	@SubscribeEvent
	public void worldSaveEventHandler(WorldEvent.Unload event)
	{
		if (!event.world.isRemote)
		{
			for (final WorldPropertiesManager manager : MCA.getInstance().playerWorldManagerMap.values())
			{
				manager.saveWorldProperties();
			}
		}
	}

	/**
	 * Fires when the world is loading. Loads world properties for every single player into memory server side only.
	 * 
	 * @param 	event	An instance of the WorldEvent.Load event.
	 */
	@SubscribeEvent
	public void worldLoadEventHandler(WorldEvent.Load event)
	{
		if (!event.world.isRemote && !MCA.getInstance().hasLoadedProperties)
		{
			final MinecraftServer server = MinecraftServer.getServer();
			final String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getWorldDirectoryName();
			final String folderName = server.isDedicatedServer() ? "ServerWorlds" : "Worlds";
			final File folderPath = new File(RadixCore.getInstance().runningDirectory + "/config/MCA/" + folderName + "/" + worldName);

			MCA.getInstance().getLogger().log("Loading world properties from " + folderPath.getAbsolutePath() +"...");

			if (!folderPath.exists())
			{
				MCA.getInstance().getLogger().log("Creating folder " + folderPath.getPath());
				folderPath.mkdirs();
			}

			for (final File propertiesFile : folderPath.listFiles())
			{
				MCA.getInstance().playerWorldManagerMap.put(propertiesFile.getName(), new WorldPropertiesManager(MCA.getInstance(), worldName, propertiesFile.getName(), WorldPropertiesList.class));
			}

			MCA.getInstance().hasLoadedProperties = true;
		}
	}

	/**
	 * Fired when the player dies.
	 * 
	 * @param 	event	An instance of the PlayerDrops event.
	 */
	@SubscribeEvent
	public void playerDropsEventHandler(PlayerDropsEvent event)
	{
		MCA.getInstance().deadPlayerInventories.put(event.entityPlayer.getCommandSenderName(), event.drops);
	}

	/**
	 * Fired when the player right-clicks something.
	 * 
	 * @param 	event	An instance of the PlayerInteractEvent.
	 */
	@SubscribeEvent
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{
		if (!event.entityPlayer.worldObj.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			final Block block = event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z);

			if (block == Blocks.lit_furnace || block == Blocks.furnace)
			{
				for (final Object obj : event.entityPlayer.worldObj.loadedEntityList)
				{
					if (obj instanceof AbstractEntity)
					{
						final AbstractEntity entity = (AbstractEntity)obj;

						if (entity.getInstanceOfCurrentChore() instanceof ChoreCooking && 
							entity.cookingChore.hasCookableFood &&
								entity.cookingChore.furnacePosX == event.x &&
								entity.cookingChore.furnacePosY == event.y &&
								entity.cookingChore.furnacePosZ == event.z)
						{
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}

	/**
	 * Fired when the player interacts with an entity.
	 * 
	 * @param 	event	An instance of the EntityInteractEvent.
	 */
	@SubscribeEvent
	public void entityInteractEventHandler(EntityInteractEvent event)
	{
		//Make it so that when right clicking a horse that an MCA villager is riding,
		//open up that villager's interaction GUI.
		if (event.target instanceof EntityHorse)
		{
			final EntityHorse entityHorse = (EntityHorse)event.target;
			
			if (entityHorse.riddenByEntity instanceof AbstractEntity)
			{
				final AbstractEntity entity = (AbstractEntity)entityHorse.riddenByEntity;
				entity.interact(event.entityPlayer);
			}
		}
		
		else if (event.target instanceof EntityPlayer && canInteractWithPlayer(event.entityPlayer) && !event.entityPlayer.worldObj.isRemote)
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(event.target.getEntityId(), Constants.ID_GUI_PLAYER), (EntityPlayerMP)event.entityPlayer);
		}
	}
	
	/**
	 * Ticks the server tick handler.
	 * 
	 * @param 	event	The event.
	 */
	@SubscribeEvent
	public void serverTickEventHandler(ServerTickEvent event)
	{
		MCA.getInstance().serverTickHandler.onTick();
	}
	
	/**
	 * Ticks the client tick handler.
	 * 
	 * @param 	event	The event.
	 */
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event)
	{
		MCA.getInstance().clientTickHandler.onTick();
	}

	/**
	 * Makes the server create a new world properties manager for the player and
	 * sends it to them.
	 * 
	 * @param 	event	The event.
	 */
	@SubscribeEvent
	public void playerLoggedInEventHandler(PlayerLoggedInEvent event)
	{
		final WorldPropertiesManager manager = new WorldPropertiesManager(MCA.getInstance(), event.player.worldObj.getSaveHandler().getWorldDirectoryName(), event.player.getCommandSenderName(), WorldPropertiesList.class);
		MCA.getInstance().playerWorldManagerMap.put(event.player.getCommandSenderName(), manager);
		
		MCA.packetHandler.sendPacketToPlayer(new PacketSetWorldProperties(manager), (EntityPlayerMP) event.player);
		
		if (MCA.getInstance().getWorldProperties(manager).playerName.equals(""))
		{
			MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(event.player.getEntityId(), Constants.ID_GUI_SETUP), (EntityPlayerMP)event.player);
		}
	}
	
	/**
	 * Handles crafting of a crown and setting to monarch status.
	 * 
	 * @param 	event	The event.
	 */
	@SubscribeEvent
	public void itemCraftedEventHandler(ItemCraftedEvent event)
	{
		if (event.crafting.getItem() instanceof ItemCrown && !event.player.worldObj.isRemote)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(event.player.getCommandSenderName());

			if (!MCA.getInstance().getWorldProperties(manager).isMonarch)
			{
				MCA.getInstance().getWorldProperties(manager).isMonarch = true;
				manager.saveWorldProperties();

				MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(event.player, null, "notify.monarch.began", false, null, null), (EntityPlayerMP)event.player);
				event.player.triggerAchievement(MCA.getInstance().achievementCraftCrown);
			}
		}
	}
	
	/**
	 * Handles the smelting of a baby.
	 * 
	 * @param 	event	The event.
	 */
	@SubscribeEvent
	public void itemSmeltedEventHandler(ItemSmeltedEvent event)
	{
		if (event.smelting.getItem() instanceof ItemTombstone && !event.player.worldObj.isRemote)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(event.player.getCommandSenderName());

			//Reset all information about the baby.
			MCA.getInstance().getWorldProperties(manager).babyExists = false;
			MCA.getInstance().getWorldProperties(manager).babyIsMale = false;
			MCA.getInstance().getWorldProperties(manager).babyName = "";
			MCA.getInstance().getWorldProperties(manager).babyReadyToGrow = false;
			MCA.getInstance().getWorldProperties(manager).minutesBabyExisted = 0;

			manager.saveWorldProperties();

			MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(event.player, null, "notify.baby.cooked", false, null, null), (EntityPlayerMP)event.player);
			event.player.triggerAchievement(MCA.getInstance().achievementCookBaby);
		}
	}
	
	private void doAddMobTasks(EntityMob mob)
	{
		if (mob instanceof EntityEnderman)
		{
			return;
		}

		else if (mob instanceof EntityCreeper)
		{
			mob.tasks.addTask(0, new EntityAIAvoidEntity(mob, EntityVillager.class, 16F, 1.35F, 1.35F));
		}

		else
		{
			float moveSpeed = 0.7F;

			if (mob instanceof EntitySpider)
			{
				moveSpeed = 1.2F;
			}

			else if (mob instanceof EntitySkeleton)
			{
				moveSpeed = 1.1F;
			}

			else if (mob instanceof EntityZombie)
			{
				moveSpeed = 0.9F;
			}

			mob.tasks.addTask(2, new EntityAIAttackOnCollide(mob, EntityVillagerAdult.class, moveSpeed, false));
			mob.tasks.addTask(2, new EntityAIAttackOnCollide(mob, EntityPlayerChild.class, moveSpeed, false));
			mob.tasks.addTask(2, new EntityAIAttackOnCollide(mob, EntityVillagerChild.class, moveSpeed, false));
			mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityVillagerAdult.class, 16, false));
			mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityPlayerChild.class, 16, false));
			mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityVillagerChild.class, 16, false));
		}
	}

	private void doOverwriteVillager(EntityJoinWorldEvent event, EntityVillager villager)
	{
		//Only run this if the profession of the villager being spawned is one of the six
		//original professions or one of mod-registered professions
		if ((villager.getProfession() < 6 && villager.getProfession() > -1)
				|| VillagerRegistryMCA.getRegisteredVillagersMap().containsKey(villager.getProfession()))
		{
			//Cancel the spawn.
			event.setCanceled(true);

			int newProfession = 0;

			//Factor in MCA's added professions.
			switch (villager.getProfession())
			{
			case 0:
				if (Utility.getBooleanWithProbability(30))
				{
					newProfession = 7;
				}

				else
				{
					newProfession = villager.getProfession();
				}

				break;

			case 4: 
				if (Utility.getBooleanWithProbability(50))
				{
					newProfession = 6;
				}

				break;

			default:
				newProfession = villager.getProfession();
				break;
			}

			//Create the replacement villager and set its location.
			final EntityVillagerAdult newVillager = new EntityVillagerAdult(villager.worldObj, newProfession);
			newVillager.setPositionAndRotation(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);

			villager.worldObj.spawnEntityInWorld(newVillager);
			villager.setDead();
		}
	}
	
	private boolean canInteractWithPlayer(EntityPlayer player)
	{
		final ItemStack itemStack = player.getHeldItem();
		
		if (itemStack != null)
		{
			final Item heldItem = itemStack.getItem();
			
			if (heldItem instanceof ItemWeddingRing)
			{
				return true;
			}
			
			return false;
		}
		
		else
		{
			return true;
		}
	}
}

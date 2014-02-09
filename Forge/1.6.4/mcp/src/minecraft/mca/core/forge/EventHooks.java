/*******************************************************************************
 * EventHooks.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.io.File;

import mca.chore.ChoreCooking;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.AbstractSerializableEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.item.AbstractBaby;
import mca.item.ItemWeddingRing;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

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
	@ForgeSubscribe
	public void itemTossedEventHandler(ItemTossEvent event)
	{
		if (event.entityItem.getEntityItem().getItem() instanceof AbstractBaby && MCA.getInstance().playerWorldManagerMap.get(event.player.username).worldProperties.babyExists)
		{
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createSayLocalizedPacket(event.player, null, "notify.player.droppedbaby", false, null, null), (Player)event.player);
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
	@ForgeSubscribe
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
	 * Fires when the world is loading. Loads world properties for every single player into memory server side only.
	 * 
	 * @param 	event	An instance of the WorldEvent.Load event.
	 */
	@ForgeSubscribe
	public void worldLoadEventHandler(WorldEvent.Load event)
	{
		if (!event.world.isRemote && !MCA.getInstance().hasLoadedProperties)
		{
			final MinecraftServer server = MinecraftServer.getServer();
			final String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getWorldDirectoryName();
			final String folderName = server.isDedicatedServer() ? "ServerWorlds" : "Worlds";
			final File folderPath = new File(MCA.getInstance().runningDirectory + "/config/MCA/" + folderName + "/" + worldName);

			MCA.getInstance().log("Loading world properties from " + folderPath.getAbsolutePath() +"...");

			if (!folderPath.exists())
			{
				MCA.getInstance().log("Creating folder " + folderPath.getPath());
				folderPath.mkdirs();
			}

			for (final File propertiesFile : folderPath.listFiles())
			{
				MCA.getInstance().playerWorldManagerMap.put(propertiesFile.getName(), new WorldPropertiesManager(worldName, propertiesFile.getName()));
			}

			MCA.getInstance().hasLoadedProperties = true;
		}
	}

	/**
	 * Fires when the world is saving.
	 * 
	 * @param 	event	An instance of the WorldEvent.Unload event.
	 */
	@ForgeSubscribe
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
	 * Fired when the player dies.
	 * 
	 * @param 	event	An instance of the PlayerDrops event.
	 */
	@ForgeSubscribe
	public void playerDropsEventHandler(PlayerDropsEvent event)
	{
		MCA.getInstance().deadPlayerInventories.put(event.entityPlayer.username, event.drops);
	}

	/**
	 * Fired when the player right-clicks something.
	 * 
	 * @param 	event	An instance of the PlayerInteractEvent.
	 */
	@ForgeSubscribe
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{
		if (!event.entityPlayer.worldObj.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			final int blockId = event.entityPlayer.worldObj.getBlockId(event.x, event.y, event.z);

			if (blockId == Block.furnaceBurning.blockID || blockId == Block.furnaceIdle.blockID)
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
	@ForgeSubscribe
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
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(event.target.entityId, Constants.ID_GUI_PLAYER), (Player)event.entityPlayer);
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
		//original professions.
		if (villager.getProfession() < 6 && villager.getProfession() > -1)
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

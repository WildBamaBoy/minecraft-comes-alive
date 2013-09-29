/*******************************************************************************
 * EventHooks.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.core.forge;

import java.io.File;

import mods.mca.core.MCA;
import mods.mca.core.io.WorldPropertiesManager;
import mods.mca.core.util.PacketHelper;
import mods.mca.entity.AbstractEntity;
import mods.mca.entity.AbstractSerializableEntity;
import mods.mca.entity.EntityPlayerChild;
import mods.mca.entity.EntityVillagerAdult;
import mods.mca.entity.EntityVillagerChild;
import mods.mca.item.ItemBaby;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
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
		if (event.entityItem.getEntityItem().getItem() instanceof ItemBaby)
		{
			if (MCA.instance.playerWorldManagerMap.get(event.player.username).worldProperties.babyExists)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHelper.createSayLocalizedPacket(event.player, null, "notify.player.droppedbaby", false, null, null), (Player)event.player);
				event.player.inventory.addItemStackToInventory(event.entityItem.getEntityItem());

				if (event.entity.worldObj.isRemote)
				{
					PacketDispatcher.sendPacketToServer(PacketHelper.createAddItemPacket(event.entityItem.getEntityItem().itemID, event.player.entityId));
				}

				event.setCanceled(true);
			}
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
		if (event.entity instanceof EntityMob)
		{
			EntityMob mob = (EntityMob)event.entity;

			if (!(mob instanceof EntityEnderman) && !(mob instanceof EntityCreeper))
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
//				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityVillagerAdult.class, 16, 50, false));
//				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityPlayerChild.class, 16, 50, false));
//				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityVillagerChild.class, 16, 50, false));
			}

			else if (mob instanceof EntityCreeper)
			{
				mob.tasks.addTask(0, new EntityAIAvoidEntity(mob, EntityVillager.class, 16F, 1.35F, 1.35F));
			}
		}

		if (!event.world.isRemote)
		{
			//Now check the event's entity and see if it is a Testificate.
			if (event.entity instanceof EntityVillager && !(event.entity instanceof AbstractSerializableEntity))
			{
				EntityVillager villager = (EntityVillager)event.entity;

				//Only run this if the profession of the villager being spawned is one of the six
				//original professions.
				if (villager.getProfession() < 6 && villager.getProfession() > -1)
				{
					//Cancel the spawn.
					event.setCanceled(true);

					int newVillagerProfession = 0;

					//Factor in MCA's added professions.
					switch (villager.getProfession())
					{
					case 0:
						if (AbstractEntity.getBooleanWithProbability(30))
						{
							newVillagerProfession = 7;
						}

						else
						{
							newVillagerProfession = villager.getProfession();
						}

						break;

					case 4: 
						if (AbstractEntity.getBooleanWithProbability(50))
						{
							newVillagerProfession = 6;
						}

						break;

					default:
						newVillagerProfession = villager.getProfession();
						break;
					}

					//Create the replacement villager and set its location.
					EntityVillagerAdult newVillager = new EntityVillagerAdult(event.world, newVillagerProfession);
					newVillager.setPositionAndRotation(villager.posX, villager.posY, villager.posZ, villager.rotationYaw, villager.rotationPitch);

					//Kill the Testificate.
					villager.setDead();

					//Spawn the new villager.
					event.world.spawnEntityInWorld(newVillager);
				}
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
		if (!event.world.isRemote && !MCA.instance.hasLoadedProperties)
		{
			MinecraftServer server = MinecraftServer.getServer();

			if (server.isDedicatedServer())
			{
				MCA.instance.log("Loading world properties for dedicated server...");

				String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getSaveDirectoryName();
				File worldPropertiesFolderPath = new File(MCA.instance.runningDirectory + "/config/MCA/ServerWorlds/" + worldName);

				if (!worldPropertiesFolderPath.exists())
				{
					MCA.instance.log("Creating folder " + worldPropertiesFolderPath.getPath());
					worldPropertiesFolderPath.mkdirs();
				}

				for (File file : worldPropertiesFolderPath.listFiles())
				{
					MCA.instance.playerWorldManagerMap.put(file.getName(), new WorldPropertiesManager(worldName, file.getName()));
				}
			}

			else
			{
				MCA.instance.log("Loading world properties for integrated server...");

				String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getSaveDirectoryName();
				File worldPropertiesFolderPath = new File(MCA.instance.runningDirectory + "/config/MCA/Worlds/" + worldName);

				if (!worldPropertiesFolderPath.exists())
				{
					MCA.instance.log("Creating folder " + worldPropertiesFolderPath.getPath());
					worldPropertiesFolderPath.mkdirs();
				}

				for (File file : worldPropertiesFolderPath.listFiles())
				{
					MCA.instance.playerWorldManagerMap.put(file.getName(), new WorldPropertiesManager(worldName, file.getName()));
				}
			}

			MCA.instance.hasLoadedProperties = true;
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
		for (WorldPropertiesManager manager : MCA.instance.playerWorldManagerMap.values())
		{
			manager.saveWorldProperties();
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
		MCA.instance.deadPlayerInventories.put(event.entityPlayer.username, event.drops);
	}
}

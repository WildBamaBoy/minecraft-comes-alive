/*******************************************************************************
 * PacketHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mca.chore.AbstractChore;
import mca.chore.ChoreCombat;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.ModPropertiesManager;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.object.FamilyTree;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityChild;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.enums.EnumTrait;
import mca.inventory.Inventory;
import mca.item.ItemBaby;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.NetworkListenThread;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Handles packets received both client and server side.
 */
public final class PacketHandler implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		try
		{
			if (MCA.getInstance().inDebugMode & MCA.getInstance().debugDoLogPackets)
			{
				MCA.getInstance().logPacketInformation("Received packet: " + packet.channel + ". Size = " + packet.length);
			}
			
			if (packet.channel.equals("MCA_F_REQ"))
			{
				handleFieldRequest(packet, player);
			}

			else if (packet.channel.equals("MCA_F_VAL"))
			{
				handleFieldValue(packet, player);
			}

			else if (packet.channel.equals("MCA_TARGET"))
			{
				handleTarget(packet, player);
			}

			else if (packet.channel.equals("MCA_REMOVEITEM"))
			{
				handleRemoveItem(packet, player);
			}

			else if (packet.channel.equals("MCA_ACHIEV"))
			{
				handleAchievement(packet, player);
			}

			else if (packet.channel.equals("MCA_SYNC"))
			{
				handleSync(packet, player);
			}

			else if (packet.channel.equals("MCA_SYNC_REQ"))
			{
				handleSyncRequest(packet, player);
			}

			else if (packet.channel.equals("MCA_ENGAGE"))
			{
				handleEngagement(packet, player);
			}

			else if (packet.channel.equals("MCA_ADDITEM"))
			{
				handleAddItem(packet, player);
			}

			else if (packet.channel.equals("MCA_FAMTREE"))
			{
				handleFamilyTree(packet, player);
			}

			else if (packet.channel.equals("MCA_DROPITEM"))
			{
				handleDropItem(packet, player);
			}

			else if (packet.channel.equals("MCA_INVENTORY"))
			{
				handleInventory(packet, player);
			}

			else if (packet.channel.equals("MCA_CHORE"))
			{
				handleChore(packet, player);
			}

			else if (packet.channel.equals("MCA_TOMB"))
			{
				handleTombstone(packet, player);
			}

			else if (packet.channel.equals("MCA_TOMB_REQ"))
			{
				handleTombstoneRequest(packet, player);
			}

			else if (packet.channel.equals("MCA_POSITION"))
			{
				handlePosition(packet, player);
			}

			else if (packet.channel.equals("MCA_REMOVEITEM"))
			{
				handleRemoveItem(packet, player);
			}

			else if (packet.channel.equals("MCA_KILL"))
			{
				handleKill(packet, player);
			}

			else if (packet.channel.equals("MCA_LOGIN"))
			{
				handleLogin(packet, player);
			}

			else if (packet.channel.equals("MCA_WORLDPROP"))
			{
				handleWorldProperties(packet, player);
			}

			else if (packet.channel.equals("MCA_SAYLOCAL"))
			{
				handleSayLocalized(packet, player);
			}

			else if (packet.channel.equals("MCA_PLMARRY"))
			{
				handlePlayerMarried(packet, player);
			}

			else if (packet.channel.equals("MCA_HAVEBABY"))
			{
				handleHaveBaby(packet, player);
			}

			else if (packet.channel.equals("MCA_BABYINFO"))
			{
				handleBabyInfo(packet, player);
			}

			else if (packet.channel.equals("MCA_TRADE"))
			{
				handleTrade(packet, player);
			}

			else if (packet.channel.equals("MCA_RESPAWN"))
			{
				handleRespawn(packet, player);
			}

			else if (packet.channel.equals("MCA_VPPROC"))
			{
				handleVillagerPlayerProcreate(packet, player);
			}

			else if (packet.channel.equals("MCA_ADDAI"))
			{
				handleAddAI(packet, player);
			}
			
			else if (packet.channel.equals("MCA_RETURNINV"))
			{
				handleReturnInventory(packet, player);
			}
		}

		catch (Exception e)
		{
			MCA.getInstance().log(e);
		}
	}

	/**
	 * Creates packet used to request the value of a field.
	 * 
	 * @param 	entityId	The id of the entity that contains the field.
	 * @param 	fieldName	The name of the field being requested.
	 * 
	 * @return	A field request packet.
	 */
	public static Packet createFieldRequestPacket(int entityId, String fieldName)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_F_REQ";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(fieldName);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that requests the value of a field. This kind of packet is only sent by the client.
	 * 
	 * @param 	packet	The packet containing the field request data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleFieldRequest(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException, IllegalAccessException, SecurityException, NoSuchFieldException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId     = (Integer)objectInput.readObject();
		String fieldName = (String)objectInput.readObject();

		//Loop through server entities and send the field's value to the player.
		for (Object obj : world.loadedEntityList)
		{
			Entity entity = (Entity)obj;

			if (entity.entityId == entityId)
			{
				//Workaround for protected field "texture".
				if (fieldName.equals("texture"))
				{
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createFieldValuePacket(entityId, fieldName, ((AbstractEntity)entity).getTexture()), player);
					break;
				}

				else
				{
					Field field = entity.getClass().getField(fieldName);
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createFieldValuePacket(entityId, fieldName, field.get(entity)), player);
					break;
				}
			}
		}
	}

	/**
	 * Creates packet used to assign the value of a field.
	 * 
	 * @param 	entityId	The id of the entity that contains the field to be changed.
	 * @param 	fieldName	The name of the field to be changed.
	 * @param 	fieldValue	The new value of the field.
	 * 
	 * @return	A field value packet.
	 */
	public static Packet createFieldValuePacket(int entityId, String fieldName, Object fieldValue)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_F_VAL";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(fieldName);
			objectOutput.writeObject(fieldValue);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that contains the value that a field should be changed to.
	 * 
	 * @param 	packet	The packet containing the field value data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleFieldValue(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World world = entityPlayer.worldObj;

		//Assign received data.
		int entityId     = (Integer)objectInput.readObject();
		String fieldName = (String)objectInput.readObject();
		Object fieldValue = objectInput.readObject();

		for (Object obj : world.loadedEntityList)
		{
			Entity entity = (Entity)obj;

			if (entity.entityId == entityId)
			{
				for (Field f : entity.getClass().getFields())
				{
					if (!fieldName.equals("texture"))
					{
						if (f.getName().equals(fieldName))
						{
							if (f.getType().getName().contains("boolean"))
							{
								entity.getClass().getField(fieldName).set(entity, Boolean.parseBoolean(fieldValue.toString()));

								//Special condition. When isSpouse is changed, a villager's AI must be updated just in case it is a guard who is
								//either getting married or getting divorced.
								if (f.getName().equals("isSpouse"))
								{
									AbstractEntity abstractEntity = (AbstractEntity)entity;
									abstractEntity.addAI();
								}
							}

							else if (f.getType().getName().contains("int"))
							{
								entity.getClass().getField(fieldName).set(entity, Integer.parseInt(fieldValue.toString()));

								if (f.getName().equals("traitId"))
								{
									AbstractEntity abstractEntity = (AbstractEntity)entity;
									abstractEntity.trait = EnumTrait.getTraitById(abstractEntity.traitId);
								}

								if (f.getName().equals("profession"))
								{
									AbstractEntity abstractEntity = (AbstractEntity)entity;
									abstractEntity.addAI();
								}
							}

							else if (f.getType().getName().contains("double"))
							{
								entity.getClass().getField(fieldName).set(entity, Double.parseDouble(fieldValue.toString()));
							}

							else if (f.getType().getName().contains("float"))
							{
								entity.getClass().getField(fieldName).set(entity, Float.parseFloat(fieldValue.toString()));

								AbstractEntity abstractEntity = (AbstractEntity)entity;
								abstractEntity.setMoodByMoodPoints(false);
							}

							else if (f.getType().getName().contains("String"))
							{
								entity.getClass().getField(fieldName).set(entity, fieldValue.toString());
							}

							else if (f.getType().getName().contains("Map"))
							{
								if (f.getName().equals("playerMemoryMap"))
								{
									//Player name must be set if the map is a memory map since it is transient.
									Map<String, PlayerMemory> memoryMap = (Map<String, PlayerMemory>)fieldValue;
									PlayerMemory memory = memoryMap.get(entityPlayer.username);

									if (memory != null)
									{
										memory.playerName = entityPlayer.username;
										memoryMap.put(entityPlayer.username, memory);
									}

									else
									{
										memoryMap.put(entityPlayer.username, new PlayerMemory(entityPlayer.username));
									}

									entity.getClass().getField(fieldName).set(entity, memoryMap);
								}

								else
								{
									entity.getClass().getField(fieldName).set(entity, fieldValue);
								}
							}
						}
					}

					else
					{
						((AbstractEntity)entity).setTexture(fieldValue.toString());
					}
				}

				break;
			}
		}

		//Sync with all other players if server side.
		if (!world.isRemote)
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSyncPacket((AbstractEntity) world.getEntityByID(entityId)));
		}
	}

	/**
	 * Creates packet used to set an entity's target.
	 * 
	 * @param 	entityId	The id of the entity the target is being assigned to.
	 * @param 	targetId	The target entity's ID.
	 * 
	 * @return	A set target packet.
	 */
	public static Packet createSetTargetPacket(int entityId, int targetId)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_TARGET";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(targetId);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet meant to assign a target to an entity.
	 * 
	 * @param 	packet	The packet containing the target data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleTarget(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = (Integer)objectInput.readObject();
		int targetId = (Integer)objectInput.readObject();

		for (Object obj : world.loadedEntityList)
		{
			Entity entity = (Entity)obj;

			if (entity.entityId == entityId)
			{
				AbstractEntity genderizedEntity = (AbstractEntity)entity;

				if (targetId == 0)
				{
					genderizedEntity.target = null;
				}

				else
				{
					genderizedEntity.target = (EntityLivingBase)genderizedEntity.worldObj.getEntityByID(targetId);
				}
			}
		}
	}

	/**
	 * Creates packet used to remove one of an item from the player's inventory.
	 * 
	 * @param 	playerID	The id of the player to remove the item from.
	 * @param	slotID		The slot id that the item is contained in.
	 * @param	quantity	The amount of the item to remove.
	 * @param	damage		The damage of the item.
	 * 
	 * @return	A gift packet.
	 */
	public static Packet createRemoveItemPacket(int playerID, int slotID, int quantity, int damage)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_REMOVEITEM";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(playerID);
			objectOutput.writeObject(slotID);
			objectOutput.writeObject(quantity);
			objectOutput.writeObject(damage);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that removes an item from the player's inventory.
	 * 
	 * @param 	packet	The packet containing the item and player data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleRemoveItem(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		//Assign received data.
		int playerID = (Integer)objectInput.readObject();
		int slotID = (Integer)objectInput.readObject();
		int quantity = (Integer)objectInput.readObject();
		int damage = (Integer)objectInput.readObject();

		EntityPlayer entityPlayer = (EntityPlayer)player;
		ItemStack itemStack = entityPlayer.inventory.mainInventory[slotID];

		//Consume one of that item in the player's inventory.
		int nextStackSize = itemStack.stackSize - quantity;

		//Check if the next size is zero or below, meaning it must be null.
		if (nextStackSize <= 0)
		{
			entityPlayer.inventory.setInventorySlotContents(slotID, null);
		}

		//The new stack size is greater than zero.
		else
		{
			ItemStack newItemStack = new ItemStack(itemStack.getItem(), nextStackSize, damage);
			entityPlayer.inventory.setInventorySlotContents(slotID, newItemStack);
		}
	}

	/**
	 * Creates packet used to unlock an achievement for a player.
	 * 
	 * @param 	achievement				The achievement to unlock.
	 * @param 	playerId				The id of the player to unlock the achievement on. 
	 * 
	 * @return	An achievement packet.
	 */
	public static Packet createAchievementPacket(Achievement achievement, int playerId) 
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_ACHIEV";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(achievement.statId);
			objectOutput.writeObject(playerId);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that unlocks an achievement for a player.
	 * 
	 * @param 	packet	The packet containing the achievement and player data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleAchievement(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		//Assign received data.
		int achievementID = (Integer)objectInput.readObject();
		int playerID = (Integer)objectInput.readObject();

		EntityPlayer entityPlayer = (EntityPlayer)player;

		for (Object obj : AchievementList.achievementList)
		{
			Achievement achievement = (Achievement)obj;

			if (achievement.statId == achievementID)
			{
				entityPlayer.triggerAchievement(achievement);
				break;
			}
		}
	}

	/**
	 * Creates a packet used to request synchronization of a client side entity.
	 * 
	 * @param 	entityId	The id of the client side entity that must be synced.
	 * 
	 * @return	A sync request packet.
	 */
	public static Packet createSyncRequestPacket(int entityId)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_SYNC_REQ";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that requests synchronization of a client and server side entity.
	 * 
	 * @param 	packet	The packet containing the sync request data.
	 * @param 	player	The player that send the sync request.
	 */
	@SuppressWarnings("javadoc")
	private static void handleSyncRequest(Packet250CustomPayload packet, Player player) throws IllegalArgumentException, IllegalAccessException, IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = Integer.valueOf(objectInput.readObject().toString());
		objectInput.close();

		for (Object obj : world.loadedEntityList)
		{
			if (obj instanceof AbstractEntity)
			{
				AbstractEntity entity = (AbstractEntity)obj;

				if (entity.entityId == entityId)
				{
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createSyncPacket(entity), player);
					break;
				}
			}
		}
	}

	/**
	 * Creates a packet used to synchronize a client side entity with the server side one.
	 * 
	 * @param 	abstractEntity	The id of the server side entity that will be sent to the client.
	 * 
	 * @return	A sync packet.
	 */
	public static Packet createSyncPacket(AbstractEntity abstractEntity)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_SYNC";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(abstractEntity);
			objectOutput.writeObject(abstractEntity.entityId);
			objectOutput.writeObject(abstractEntity.getTexture());
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that contains synchronization data.
	 * 
	 * @param 	packet	The packet containing the synchronization data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	@SideOnly(Side.CLIENT)
	private static void handleSync(Packet250CustomPayload packet, Player player) throws IllegalArgumentException, IllegalAccessException, IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		WorldClient world = Minecraft.getMinecraft().theWorld;

		//Assign received data.
		AbstractEntity receivedEntity = (AbstractEntity)objectInput.readObject();
		int receivedId = (Integer)objectInput.readObject();
		String receivedTexture = objectInput.readObject().toString();
		objectInput.close();

		AbstractEntity clientEntity = null;

		//Get the client side entity.
		clientEntity = (AbstractEntity)world.getEntityByID(receivedId);

		if (clientEntity.entityId == receivedId)
		{
			//Figure out which classes the entity is composed of.
			List<Class> classList = new ArrayList<Class>();
			classList.add(AbstractEntity.class);
			classList.add(receivedEntity.getClass());

			if (receivedEntity instanceof EntityPlayerChild || receivedEntity instanceof EntityVillagerChild)
			{
				classList.add(EntityChild.class);
			}

			//Loop through each field in each class that the received entity is made of and set the value of
			//the field on the client side entity to the same value as the received entity.
			for (Class c : classList)
			{
				for (Field f : c.getDeclaredFields())
				{
					try
					{
						//Workaround for chores not being assigned an owner.
						if (f.get(receivedEntity) instanceof AbstractChore)
						{
							AbstractChore theChore = (AbstractChore)f.get(receivedEntity);
							theChore.owner = clientEntity;

							f.set(clientEntity, theChore);
						}

						//Workaround for family tree not being assigned an owner.
						else if (f.get(receivedEntity) instanceof FamilyTree)
						{
							FamilyTree theFamilyTree = (FamilyTree)f.get(receivedEntity);
							theFamilyTree.owner = clientEntity;

							f.set(clientEntity, theFamilyTree);
						}

						//Workaround for inventory not being assigned an owner.
						else if (f.get(receivedEntity) instanceof Inventory)
						{
							Inventory theInventory = (Inventory)f.get(receivedEntity);
							theInventory.owner = clientEntity;

							if (!clientEntity.inventory.equals(theInventory))
							{
								f.set(clientEntity, theInventory);
							}
						}

						else if (!Modifier.isFinal(f.getModifiers()) && !Modifier.isTransient(f.getModifiers()))
						{
							f.set(clientEntity, f.get(receivedEntity));
						}
					}

					catch (IllegalAccessException e)
					{
						continue;
					}
				}
			}

			//Tell the client entity that it no longer needs to sync and give it the received entity's texture.
			clientEntity.setTexture(receivedTexture);

			//Put the client entity's ID in the ids map.
			MCA.getInstance().idsMap.put(clientEntity.mcaID, clientEntity.entityId);
			MCA.getInstance().entitiesMap.put(clientEntity.mcaID, clientEntity);
			
			//Set the entity mood and trait.
			clientEntity.setMoodByMoodPoints(false);
			clientEntity.trait = EnumTrait.getTraitById(clientEntity.traitId);

			//Add the client entity's AI.
			clientEntity.addAI();

			return;
		}
	}

	/**
	 * Creates a packet used to notify all surrounding villagers of an engagement, making them give the player gifts.
	 * 
	 * @param 	entityId	The id of the entity getting engaged.
	 * 
	 * @return	An engagement packet.
	 */
	public static Packet createEngagementPacket(int entityId)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_ENGAGE";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that makes villagers have gifts for player who are engaged.
	 * 
	 * @param 	packet	The packet containing the engagement data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleEngagement(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException 
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World world = entityPlayer.worldObj;

		//Assign received data.
		int entityId = (Integer)objectInput.readObject();

		List<Entity> entitiesAroundMe = LogicHelper.getAllEntitiesWithinDistanceOfEntity(world.getEntityByID(entityId), 64);

		for (Entity entity : entitiesAroundMe)
		{
			if (entity instanceof EntityVillagerAdult)
			{
				EntityVillagerAdult entityVillager = (EntityVillagerAdult)entity;

				if (entityVillager.playerMemoryMap.containsKey(entityPlayer.username))
				{
					PlayerMemory memory = entityVillager.playerMemoryMap.get(entityPlayer.username);
					memory.hasGift = true;
					entityVillager.playerMemoryMap.put(entityPlayer.username, memory);
				}
			}
		}
	}

	/**
	 * Creates a packet used to add an item to the provided player's inventory.
	 * 
	 * @param 	itemId		The id of the item to add.
	 * @param 	playerId	The id of the player that is receiving the item.
	 * 
	 * @return	An add item packet.
	 */
	public static Packet createAddItemPacket(int itemId, int playerId)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_ADDITEM";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(itemId);
			objectOutput.writeObject(playerId);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that adds an item to the player's inventory.
	 * 
	 * @param 	packet	The packet containing the item data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleAddItem(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException 
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		//Assign received data.
		int itemId = (Integer)objectInput.readObject();
		int playerId = (Integer)objectInput.readObject();

		EntityPlayer entityPlayer = (EntityPlayer)player;
		entityPlayer.inventory.addItemStackToInventory(new ItemStack(itemId, 1, 0));
	}

	/**
	 * Creates a packet used to update a family tree across the client and server.
	 * 
	 * @param 	entityId	The id of the entity that owns the family tree.
	 * @param 	familyTree	The family tree to send to the entity.
	 * 
	 * @return	A family tree packet.
	 */
	public static Packet createFamilyTreePacket(int entityId, FamilyTree familyTree)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_FAMTREE";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(familyTree);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that updates a family tree.
	 * 
	 * @param	packet	The packet containing the family tree data.
	 * @param 	player	The player that sent the packet.
	 */
	@SuppressWarnings("javadoc")
	private static void handleFamilyTree(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = Integer.valueOf(objectInput.readObject().toString());
		FamilyTree familyTree = (FamilyTree)objectInput.readObject();

		objectInput.close();

		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);
		familyTree.owner = entity;
		entity.familyTree = familyTree;
	}

	/**
	 * Creates a packet used to make an entity drop an item not within their inventory.
	 * 
	 * @param 	entityId	The id of the entity dropping the items.
	 * @param 	itemId		The id of the item to drop.
	 * @param 	count		The amount of the item to drop.
	 * 
	 * @return	A drop item packet.
	 */
	public static Packet createDropItemPacket(int entityId, int itemId, int count)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_DROPITEM";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(itemId);
			objectOutput.writeObject(count);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that makes an entity drop an item that is not in its inventory.
	 * 
	 * @param 	packet	The packet containing the drop item data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleDropItem(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException 
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = (Integer)objectInput.readObject();
		int itemId = (Integer)objectInput.readObject();
		int count = (Integer)objectInput.readObject();

		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);
		entity.dropItem(itemId, count);
	}

	/**
	 * Creates a packet used to update an inventory across the client and server.
	 * 
	 * @param 	entityId	The id of the entity who owns the inventory.
	 * @param 	inventory	The inventory to send to the entity.
	 * 
	 * @return	An inventory packet.
	 */
	public static Packet createInventoryPacket(int entityId, Inventory inventory)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_INVENTORY";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(inventory);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that updates an inventory.
	 * 
	 * @param	packet	The packet containing the inventory data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleInventory(Packet250CustomPayload packet, Player player) throws NumberFormatException, IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = Integer.valueOf(objectInput.readObject().toString());
		Inventory inventory = (Inventory)objectInput.readObject();
		objectInput.close();

		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);
		inventory.owner = entity;
		entity.inventory = inventory;
	}

	/**
	 * Creates a packet used to update a chore across the client and server.
	 * 
	 * @param 	entityId	The id of the entity who owns the chore.
	 * @param 	chore		The chore to send to the entity.
	 * 
	 * @return	A chore packet.
	 */
	public static Packet createChorePacket(int entityId, AbstractChore chore) 
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_CHORE";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entityId);
			objectOutput.writeObject(chore);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that updates a chore.
	 * 
	 * @param	packet	The packet containing the chore data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleChore(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = (Integer)objectInput.readObject();
		AbstractChore chore = (AbstractChore)objectInput.readObject();
		objectInput.close();

		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);
		chore.owner = entity;

		if (chore instanceof ChoreFarming)
		{
			entity.farmingChore = (ChoreFarming) chore;
		}

		else if (chore instanceof ChoreWoodcutting)
		{
			entity.woodcuttingChore = (ChoreWoodcutting) chore;
		}

		else if (chore instanceof ChoreFishing)
		{
			entity.fishingChore = (ChoreFishing) chore;
		}

		else if (chore instanceof ChoreMining)
		{
			entity.miningChore = (ChoreMining) chore;
		}

		else if (chore instanceof ChoreCombat)
		{
			entity.combatChore = (ChoreCombat) chore;
		}

		else if (chore instanceof ChoreHunting)
		{
			entity.huntingChore = (ChoreHunting) chore;
		}

		else
		{
			MCA.getInstance().log("Unidentified chore type received when handling chore packet.");
		}
	}

	/**
	 * Creates a packet used to synchronize the text on a tombstone across the client and server.
	 * 
	 * @param	tombstone	The tileEntity of the tombstone being synchronized.
	 * 
	 * @return	A tombstone packet.
	 */
	public static Packet createTombstonePacket(TileEntityTombstone tombstone)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_TOMB";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(tombstone.xCoord);
			objectOutput.writeObject(tombstone.yCoord);
			objectOutput.writeObject(tombstone.zCoord);
			objectOutput.writeObject(tombstone.signText[0]);
			objectOutput.writeObject(tombstone.signText[1]);
			objectOutput.writeObject(tombstone.signText[2]);
			objectOutput.writeObject(tombstone.signText[3]);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that updates the text on a tombstone.
	 * 
	 * @param	packet	The packet that contains the tombstone data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleTombstone(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int xCoord = (Integer) objectInput.readObject();
		int yCoord = (Integer) objectInput.readObject();
		int zCoord = (Integer) objectInput.readObject();
		String line1 = (String) objectInput.readObject();
		String line2 = (String) objectInput.readObject();
		String line3 = (String) objectInput.readObject();
		String line4 = (String) objectInput.readObject();

		objectInput.close();

		TileEntityTombstone tombstone = (TileEntityTombstone)world.getBlockTileEntity(xCoord, yCoord, zCoord);
		tombstone.signText[0] = line1;
		tombstone.signText[1] = line2;
		tombstone.signText[2] = line3;
		tombstone.signText[3] = line4;
	}

	/**
	 * Creates a packet used to request the text of a tombstone at certain coordinates.
	 * 
	 * @param 	tombstone	The client-side tombstone that is requesting text.
	 * 
	 * @return	A tombstone request packet.
	 */
	public static Packet createTombstoneRequestPacket(TileEntityTombstone tombstone) 
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_TOMB_REQ";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(tombstone.xCoord);
			objectOutput.writeObject(tombstone.yCoord);
			objectOutput.writeObject(tombstone.zCoord);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that requests an update for a tombstone.
	 * 
	 * @param 	packet	The packet that contains the tombstone request data.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleTombstoneRequest(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int xCoord = (Integer)objectInput.readObject();
		int yCoord = (Integer)objectInput.readObject();
		int zCoord = (Integer)objectInput.readObject();

		TileEntityTombstone tombstone = (TileEntityTombstone)world.getBlockTileEntity(xCoord, yCoord, zCoord);
		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createTombstonePacket(tombstone));
	}

	/**
	 * Creates a packet used to set the position of an entity.
	 * 
	 * @param 	entity	The entity whose position is being set.
	 * @param 	posX	The X position to place the entity at.
	 * @param 	posY	The Y position to place the entity at.
	 * @param 	posZ	The Z position to place the entity at.
	 * 
	 * @return 	A position packet.
	 */
	public static Packet createPositionPacket(Entity entity, double posX, double posY, double posZ)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_POSITION";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entity.entityId);
			objectOutput.writeObject(posX);
			objectOutput.writeObject(posY);
			objectOutput.writeObject(posZ);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that sets an entity's position.
	 * 
	 * @param 	packet	The packet that contains the position information.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handlePosition(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = (Integer)objectInput.readObject();
		double xCoord = (Double)objectInput.readObject();
		double yCoord = (Double)objectInput.readObject();
		double zCoord = (Double)objectInput.readObject();

		Entity entity = world.getEntityByID(entityId);
		entity.setPosition(xCoord, yCoord, zCoord);
	}

	/**
	 * Creates a packet used to kill an entity.
	 * 
	 * @param 	entity	The entity to kill.
	 * 
	 * @return	A kill packet.
	 */
	public static Packet createKillPacket(Entity entity)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_KILL";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(entity.entityId);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet that kills an entity.
	 * 
	 * @param 	packet	The packet that contains the kill information.
	 * @param 	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleKill(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;

		//Assign received data.
		int entityId = (Integer)objectInput.readObject();

		AbstractEntity entity = (AbstractEntity)world.getEntityByID(entityId);

		if (entity != null)
		{
			entity.setDeadWithoutNotification();
		}
	}

	/**
	 * Creates a packet used to log in to a server running MCA.
	 * 
	 * @param 	modPropertiesManager	An instance of the client's mod properties manager.
	 * 
	 * @return	A login packet.
	 */
	public static Packet createLoginPacket(ModPropertiesManager modPropertiesManager)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_LOGIN";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(modPropertiesManager);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a login packet.
	 * 
	 * @param 	packet	The packet containing the login information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleLogin(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;
		EntityPlayer entityPlayer = (EntityPlayer)player;

		//Assign received data.
		ModPropertiesManager modPropertiesManager = (ModPropertiesManager) objectInput.readObject();

		//Ensure item IDs are the same.
		if (modPropertiesManager.equals(MCA.getInstance().modPropertiesManager))
		{
			//Give the player a world settings manager.
			WorldPropertiesManager manager = new WorldPropertiesManager(world.getSaveHandler().getWorldDirectoryName(), entityPlayer.username);

			MCA.getInstance().playerWorldManagerMap.put(entityPlayer.username, manager);

			//Send it to the client.
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createWorldPropertiesPacket(manager), player);
		}

		else
		{
			((EntityPlayerMP)player).playerNetServerHandler.kickPlayerFromServer("Minecraft Comes Alive: Server item IDs do not match your own. You cannot log in.");
		}
	}

	/**
	 * Creates a packet used to give a client or server a player's world properties.
	 * 
	 * @param 	worldPropertiesManager	An instance of the server world properties manager.
	 * 
	 * @return	A world properties packet.
	 */
	public static Packet createWorldPropertiesPacket(WorldPropertiesManager worldPropertiesManager)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_WORLDPROP";
	
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(worldPropertiesManager);
			objectOutput.close();
	
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
	
			MCA.getInstance().logPacketInformation("Created world properties packet for " + worldPropertiesManager.worldProperties.playerName);
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
	
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a world properties packet.
	 * 
	 * @param 	packet	The packet containing the world properties information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleWorldProperties(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		World world = ((EntityPlayer)player).worldObj;
		EntityPlayer entityPlayer = (EntityPlayer)player;

		//Assign received data.
		WorldPropertiesManager manager = (WorldPropertiesManager)objectInput.readObject();
		MCA.getInstance().logPacketInformation("Received world properties manager for " + ((EntityPlayer)player).username);

		//Client side.
		if (world.isRemote)
		{
			MCA.getInstance().playerWorldManagerMap.put(entityPlayer.username, manager);
		}

		//Server side.
		else
		{
			//Update only the actual properties on the old manager to retain the ability to save.
			WorldPropertiesManager oldWorldPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(entityPlayer.username);
			oldWorldPropertiesManager.worldProperties = manager.worldProperties;

			//Put the changed manager back into the map and save it.
			MCA.getInstance().playerWorldManagerMap.put(entityPlayer.username, oldWorldPropertiesManager);
			oldWorldPropertiesManager.saveWorldProperties();
		}
	}

	/**
	 * Creates a packet used to make a client say a phrase in the correct localized form.
	 * 
	 * @param 	player				The player to receive the string passed to Localization.
	 * @param 	entity				The speaker entity passed to Localization.
	 * @param 	id					The phrase ID passed to Localization.
	 * @param 	useCharacterType	The useCharacterType boolean passed to Localization.
	 * @param	prefix				The string that must be added to the beginning of the localized string.
	 * @param	suffix				The string that must be added to the end of the localized string.
	 * 
	 * @return	A say localized packet.
	 */
	public static Packet createSayLocalizedPacket(EntityPlayer player, AbstractEntity entity, String id, boolean useCharacterType, String prefix, String suffix)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_SAYLOCAL";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
	
			if (player != null)
			{
				objectOutput.writeObject(player.username);
			}
			
			else
			{
				objectOutput.writeObject(null);
			}
			
			if (entity != null)
			{
				objectOutput.writeObject(entity.entityId);
			}
			
			else
			{
				objectOutput.writeObject(null);
			}
			
			objectOutput.writeObject(id);
			objectOutput.writeObject(useCharacterType);
			
			if (prefix != null)
			{
				objectOutput.writeObject(prefix);
			}
			
			else
			{
				objectOutput.writeObject(null);
			}
			
			if (suffix != null)
			{
				objectOutput.writeObject(suffix);
			}
			
			else
			{
				objectOutput.writeObject(null);
			}
			
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a say localized packet.
	 * 
	 * @param 	packet	The packet containing the say localized information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleSayLocalized(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		EntityPlayer receivedPlayer = null;

		//Initialize fields to be filled.
		String playerName = null;
		Integer entityId = null;
		String phraseId = null;
		Boolean useCharacterType = null;
		String prefix = null;
		String suffix = null;

		//Assign received data.
		try
		{
			playerName = (String) objectInput.readObject();

			if (worldObj.getPlayerEntityByName(playerName) != null)
			{
				receivedPlayer = worldObj.getPlayerEntityByName(playerName);
			}
		}

		catch (NullPointerException e)
		{
			MCA.getInstance().log(e);
		}

		try
		{
			entityId = (Integer) objectInput.readObject();
		}

		catch (NullPointerException e)
		{
		}

		try
		{
			phraseId = (String) objectInput.readObject();
		}

		catch (NullPointerException e)
		{
		}

		try
		{
			useCharacterType = (Boolean) objectInput.readObject();
		}

		catch (NullPointerException e)
		{
		}

		try
		{
			prefix = (String) objectInput.readObject();
		}

		catch (NullPointerException e)
		{	
		}

		try
		{
			suffix = (String) objectInput.readObject();
		}

		catch (NullPointerException e)
		{
		}

		//Get the entity that should be speaking if there is one.
		if (entityId != null)
		{
			AbstractEntity speaker = (AbstractEntity)worldObj.getEntityByID(entityId);

			if (receivedPlayer != null)
			{
				speaker.say(LanguageHelper.getString(receivedPlayer, speaker, phraseId, useCharacterType, prefix, suffix));
			}

			else
			{
				speaker.say(LanguageHelper.getString(entityPlayer, speaker, phraseId, useCharacterType, prefix, suffix));
			}
		}

		//There isn't a speaker, so just add the localized string to the player's chat log.
		else
		{
			if (receivedPlayer != null)
			{
				entityPlayer.addChatMessage(LanguageHelper.getString(receivedPlayer, null, phraseId, useCharacterType, prefix, suffix));
			}

			else
			{
				entityPlayer.addChatMessage(LanguageHelper.getString(entityPlayer, null, phraseId, useCharacterType, prefix, suffix));
			}
		}
	}

	/**
	 * Creates a packet used to notify a player that they have married another player.
	 * 
	 * @param 	playerId	The ID of the player the packet is being sent to.
	 * @param	playerName	The name of the player receiving the packet.
	 * @param 	spouseId	The ID of the player's spouse.
	 * 
	 * @return	A player marriage packet.
	 */
	public static Packet createPlayerMarriagePacket(int playerId, String playerName, int spouseId)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_PLMARRY";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(playerId);
			objectOutput.writeObject(playerName);
			objectOutput.writeObject(spouseId);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to mark a player as married.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handlePlayerMarried(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		int playerId = (Integer) objectInput.readObject();
		String playerName = (String) objectInput.readObject();
		int spouseId = (Integer) objectInput.readObject();

		objectInput.close();

		//Workaround for problems on a server.
		String displayString = LanguageHelper.getString(entityPlayer, null, "multiplayer.command.output.marry.accept", false, "\u00a7A", null);
		displayString = displayString.replace("%SpouseName%", playerName);
		entityPlayer.addChatMessage(displayString);

		entityPlayer.inventory.consumeInventoryItem(MCA.getInstance().itemWeddingRing.itemID);
	}

	/**
	 * Creates a packet used to make a player have a baby.
	 * 
	 * @param 	playerId	The entity ID of the player having the baby. 
	 * @param 	spouseId	The entity ID of the spouse of the player.
	 *  
	 * @return	A have baby packet. 
	 */
	public static Packet createHaveBabyPacket(int playerId, int spouseId)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_HAVEBABY";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(playerId);
			objectOutput.writeObject(spouseId);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to trigger a player having a baby.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleHaveBaby(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		int playerId = (Integer) objectInput.readObject();
		int spouseId = (Integer) objectInput.readObject();

		objectInput.close();

		//Trigger the name baby gui.
		ItemBaby itemBaby = null;
		boolean babyIsMale = AbstractEntity.getRandomGender();

		if (babyIsMale)
		{
			itemBaby = (ItemBaby)MCA.getInstance().itemBabyBoy;
			entityPlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);
			PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementHaveBabyBoy, playerId));
			PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementHaveBabyBoy, spouseId));
		}

		else
		{
			itemBaby = (ItemBaby)MCA.getInstance().itemBabyGirl;
			entityPlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyGirl);
			PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementHaveBabyGirl, playerId));
			PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementHaveBabyGirl, spouseId));
		}

		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(entityPlayer.username);
		manager.worldProperties.babyIsMale = babyIsMale;
		manager.worldProperties.babyExists = true;
		manager.saveWorldProperties();

		PacketDispatcher.sendPacketToServer(PacketHandler.createAddItemPacket(itemBaby.itemID, entityPlayer.entityId));
		entityPlayer.openGui(MCA.getInstance(), Constants.ID_GUI_NAMECHILD, worldObj, (int)entityPlayer.posX, (int)entityPlayer.posY, (int)entityPlayer.posZ);
	}

	/**
	 * Creates a packet used to update a player with a baby's info when it is adopted.
	 * 
	 * @param 	worldPropertiesManager	The world properties manager to send.
	 *  
	 * @return	A baby info packet.
	 */
	public static Packet createBabyInfoPacket(WorldPropertiesManager worldPropertiesManager) 
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_BABYINFO";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(worldPropertiesManager);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to transfer baby information to another player.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleBabyInfo(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		WorldPropertiesManager receivedManager = (WorldPropertiesManager) objectInput.readObject();

		objectInput.close();

		//Set the player's spouse's manager to have the same baby info.
		WorldPropertiesManager spouseManager = MCA.getInstance().playerWorldManagerMap.get(receivedManager.worldProperties.playerSpouseName);

		spouseManager.worldProperties.babyExists = receivedManager.worldProperties.babyExists;
		spouseManager.worldProperties.babyIsMale = receivedManager.worldProperties.babyIsMale;
		spouseManager.worldProperties.babyName = receivedManager.worldProperties.babyName;
		spouseManager.worldProperties.babyReadyToGrow = receivedManager.worldProperties.babyReadyToGrow;

		spouseManager.saveWorldProperties();
	}

	/**
	 * Creates a packet used to open the trading GUI.
	 * 
	 * @param 	villager	The villager on whom the trading GUI will be opened.
	 *  
	 * @return	A trade packet.
	 */
	public static Packet createTradePacket(EntityVillagerAdult villager)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_TRADE";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(villager.entityId);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to open the trade GUI.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleTrade(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		int entityId = (Integer)objectInput.readObject();

		objectInput.close();

		EntityVillagerAdult villager = (EntityVillagerAdult)worldObj.getEntityByID(entityId);
		villager.setCustomer(entityPlayer);
		entityPlayer.displayGUIMerchant(villager, villager.getTitle(MCA.getInstance().getIdOfPlayer(entityPlayer), true));
	}

	/**
	 * Creates a packet used to respawn the player in hardcore mode.
	 * 
	 * @param 	player			The player that is respawning.
	 * @param	chunkCoordsX	X chunk coordinates of the player's new respawn point.
	 * @param	chunkCoordsY	Y chunk coordinates of the player's new respawn point.
	 * @param	chunkCoordsZ	Z chunk coordinates of the player's new respawn point.
	 *  
	 * @return	A trade packet.
	 */
	public static Packet createRespawnPacket(EntityPlayer player, int chunkCoordsX, int chunkCoordsY, int chunkCoordsZ)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_RESPAWN";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(chunkCoordsX);
			objectOutput.writeObject(chunkCoordsY);
			objectOutput.writeObject(chunkCoordsZ);
			objectOutput.writeObject(player.entityId);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to force respawning of a player in hardcore mode.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleRespawn(Packet250CustomPayload packet, Player player) throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayerMP entityPlayer = (EntityPlayerMP)player;
		World worldObj = entityPlayer.worldObj;
		MinecraftServer theServer = MinecraftServer.getServer();

		int chunkCoordsX = (Integer)objectInput.readObject();
		int chunkCoordsY = (Integer)objectInput.readObject();
		int chunkCoordsZ = (Integer)objectInput.readObject();
		int playerEntityId = (Integer)objectInput.readObject();

		objectInput.close();

		NetworkListenThread networkThread = theServer.getNetworkThread();

		//Get reference to the "connections" list, which is private final.
		for (Field f : networkThread.getClass().getSuperclass().getDeclaredFields())
		{
			if (f.getType().getName().equals("java.util.List"))
			{
				f.setAccessible(true);
				List connections = (List)f.get(networkThread);

				for (Object obj : connections)
				{
					NetServerHandler serverHandler = (NetServerHandler)obj;

					if (serverHandler.playerEntity.username.equals(entityPlayer.username))
					{
						//Manually respawn the player rather than allowing the game to do it, which would delete the world in hardcore mode.
						serverHandler.playerEntity.setSpawnChunk(new ChunkCoordinates(chunkCoordsX, chunkCoordsY, chunkCoordsZ), true);
						serverHandler.playerEntity = theServer.getConfigurationManager().respawnPlayer(entityPlayer, entityPlayer.dimension, false);

						break;
					}
				}

				f.setAccessible(false);
				break;
			}
		}
	}

	/**
	 * Creates a packet used to make a villager who is procreating with a player actually have a baby.
	 * 
	 * @param	villager	The villager that is procreating with the player.
	 * @param	player		The player that is procreating with the villager.
	 * @param	babyIsMale	The gender of the baby that will be added to the villager's inventory.
	 * 
	 * @return	A VillagerPlayerProcreate packet.
	 */
	public static Packet createVillagerPlayerProcreatePacket(AbstractEntity villager, EntityPlayer player, boolean babyIsMale) 
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_VPPROC";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(villager.entityId);
			objectOutput.writeObject(player.entityId);
			objectOutput.writeObject(babyIsMale);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to create a baby when a villager and player procreate.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleVillagerPlayerProcreate(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		int villagerId = (Integer)objectInput.readObject();
		int playerId = (Integer)objectInput.readObject();
		boolean babyIsMale = (Boolean)objectInput.readObject();

		objectInput.close();

		AbstractEntity villager = (AbstractEntity)worldObj.getEntityByID(villagerId);		
		ItemBaby itemBaby = null;

		//Give the villager an appropriate baby item and unlock achievements for the player.
		if (babyIsMale)
		{
			itemBaby = (ItemBaby)MCA.getInstance().itemBabyBoy;
			villager.inventory.addItemStackToInventory(new ItemStack(itemBaby, 1));
			entityPlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);

			PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementHaveBabyBoy, entityPlayer.entityId));
			PacketDispatcher.sendPacketToServer(PacketHandler.createInventoryPacket(villagerId, villager.inventory));
		}

		else
		{
			itemBaby = (ItemBaby)MCA.getInstance().itemBabyGirl;
			villager.inventory.addItemStackToInventory(new ItemStack(itemBaby, 1));
			entityPlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyGirl);

			PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementHaveBabyGirl, entityPlayer.entityId));
			PacketDispatcher.sendPacketToServer(PacketHandler.createInventoryPacket(villagerId, villager.inventory));
		}

		//Modify the player's world properties manager.
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(entityPlayer.username);
		manager.worldProperties.babyIsMale = babyIsMale;
		manager.worldProperties.babyExists = true;
		manager.saveWorldProperties();

		//Make the entityPlayer choose a name for the baby.
		entityPlayer.openGui(MCA.getInstance(), Constants.ID_GUI_NAMECHILD, worldObj, (int)entityPlayer.posX, (int)entityPlayer.posY, (int)entityPlayer.posZ);
	}

	/**
	 * Creates a packet used to add an entity's AI across the client and server.
	 * 
	 * @param	entity	The entity who needs their AI added.
	 * 
	 * @return	An add AI packet.
	 */
	public static Packet createAddAIPacket(AbstractEntity entity)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_ADDAI";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(entity.entityId);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to add AI to a villager.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleAddAI(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		int entityId = (Integer)objectInput.readObject();

		objectInput.close();

		AbstractEntity entity = (AbstractEntity)worldObj.getEntityByID(entityId);
		entity.addAI();
	}
	
	/**
	 * Creates a packet used to drop a dead player's inventory at the entity's feet.
	 * 
	 * @param	entity	The entity that will drop the items.
	 * 
	 * @return	A return inventory packet.
	 */
	public static Packet createReturnInventoryPacket(AbstractEntity entity)
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_RETURNINV";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(entity.entityId);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.getInstance().logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Exception e)
		{
			MCA.getInstance().log(e);
			return null;
		}
	}

	/**
	 * Handles a packet used to make an heir return the player's inventory.
	 * 
	 * @param 	packet	The packet containing the required information.
	 * @param	player	The player that the packet came from.
	 */
	@SuppressWarnings("javadoc")
	private static void handleReturnInventory(Packet250CustomPayload packet, Player player) throws IOException, ClassNotFoundException
	{
		byte[] data = MCA.decompressBytes(packet.data);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		ObjectInputStream objectInput = new ObjectInputStream(byteInput);

		EntityPlayer entityPlayer = (EntityPlayer)player;
		World worldObj = entityPlayer.worldObj;

		int entityId = (Integer)objectInput.readObject();

		objectInput.close();

		AbstractEntity entity = (AbstractEntity)worldObj.getEntityByID(entityId);
		ArrayList<EntityItem> itemList = MCA.getInstance().deadPlayerInventories.get(entityPlayer.username);
		
		for (EntityItem item : itemList)
		{
			entity.entityDropItem(item.getEntityItem(), 0.3F);
		}
		
		MCA.getInstance().deadPlayerInventories.remove(entityPlayer.username);
	}
}

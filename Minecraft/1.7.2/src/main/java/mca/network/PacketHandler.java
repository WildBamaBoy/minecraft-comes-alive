/*******************************************************************************
 * PacketHandler.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.network;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mca.api.registries.ChoreRegistry;
import mca.chore.AbstractChore;
import mca.chore.ChoreCombat;
import mca.chore.ChoreCooking;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesList;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LogicExtension;
import mca.core.util.Utility;
import mca.core.util.object.FamilyTree;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.enums.EnumPacketType;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import mca.inventory.Inventory;
import mca.item.AbstractBaby;
import mca.item.ItemBabyBoy;
import mca.item.ItemBabyGirl;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.block.BlockFurnace;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.radixshock.radixcore.core.IEnforcedCore;
import com.radixshock.radixcore.logic.LogicHelper;
import com.radixshock.radixcore.network.AbstractPacketHandler;
import com.radixshock.radixcore.network.Packet;

import cpw.mods.fml.relauncher.Side;

/**
 * MCA's packet handler.
 */
public final class PacketHandler extends AbstractPacketHandler
{
	/**
	 * Constructor
	 * 
	 * @param 	mod	The owner mod.
	 */
	public PacketHandler(IEnforcedCore mod) 
	{
		super(mod);
	}

	@Override
	public void onHandlePacket(Packet packet, EntityPlayer player, Side side) 
	{
		EnumPacketType type = (EnumPacketType)packet.packetType;

		try
		{
			switch (type)
			{
			case AddAI:
				handleAddAI(packet.arguments, player);
				break;
			case AddBaby:
				handleAddBaby(packet.arguments, player);
				break;
			case ArrangedMarriageParticles:
				handleArrangedMarriageParticles(packet.arguments, player);
				break;
			case BabyInfo:
				handleBabyInfo(packet.arguments, player);
				break;
			case BroadcastKillEntity:
				handleBroadcastKillEntity(packet.arguments, player);
				break;
			case AddBabyRequest:
				handleAddBabyRequest(packet.arguments, player);
				break;
			case AddMarriageRequest:
				handleAddMarriageRequest(packet.arguments, player);
				break;
			case RemoveBabyRequest:
				handleRemoveBabyRequest(packet.arguments, player);
				break;
			case RemoveMarriageRequest:
				handleRemoveMarriageRequest(packet.arguments, player);
				break;
			case ClientSideCommand:
				handleClientSideCommand(packet.arguments, player);
				break;
			case GiveRelationshipGift:
				handleGiveEngagementGift(packet.arguments, player);
				break;
			case GiveAid:
				handleGiveAid(packet.arguments, player);
				break;
			case Engagement:
				handleEngagement(packet.arguments, player);
				break;
			case ForceRespawn:
				handleForceRespawn(packet.arguments, player);
				break;
			case GetTombstoneText:
				handleGetTombstoneText(packet.arguments, player);
				break;
			case HaveBaby:
				handleHaveBaby(packet.arguments, player);
				break;
			case KillEntity:
				handleKillEntity(packet.arguments, player);
				break;
			case MountHorse:
				handleMountHorse(packet.arguments, player);
				break;
			case NameBaby:
				handleNameBaby(packet.arguments, player);
				break;
			case NotifyPlayer:
				handleNotifyPlayer(packet.arguments, player);
				break;
			case OpenGui:
				handleOpenGui(packet.arguments, player);
				break;
			case PlayerMarriage:
				handlePlayerMarriage(packet.arguments, player);
				break;
			case RemoveItem:
				handleRemoveItem(packet.arguments, player);
				break;
			case ReturnInventory:
				handleReturnInventory(packet.arguments, player);
				break;
			case SayLocalized:
				handleSayLocalized(packet.arguments, player);
				break;
			case SetChore:
				handleSetChore(packet.arguments, player);
				break;
			case SetFamilyTree:
				handleSetFamilyTree(packet.arguments, player);
				break;
			case SetFieldValue:
				handleSetFieldValue(packet.arguments, player);
				break;
			case SetInventory:
				handleSetInventory(packet.arguments, player);
				break;
			case SetPosition:
				handleSetPosition(packet.arguments, player);
				break;
			case SetTarget:
				handleSetTarget(packet.arguments, player);
				break;
			case SetTombstoneText:
				handleSetTombstoneText(packet.arguments, player);
				break;
			case SetWorldProperties:
				handleSetWorldProperties(packet.arguments, player);
				break;
			case StartTrade:
				handleStartTrade(packet.arguments, player);
				break;
			case StopJumping:
				handleStopJumping(packet.arguments, player);
				break;
			case SwingArm:
				handleSwingArm(packet.arguments, player);
				break;
			case SyncEditorSettings:
				handleSyncEditorSettings(packet.arguments, player);
				break;
			case SyncRequest:
				handleSyncRequest(packet.arguments, player);
				break;
			case Sync:
				handleSync(packet.arguments, player);
				break;
			case UpdateFurnace:
				handleUpdateFurnace(packet.arguments, player);
				break;
			default:
				MCA.getInstance().getLogger().log("WARNING: DEFAULTED PACKET TYPE - " + packet.packetType.toString());
			}
		}

		catch (Exception e)
		{
			MCA.getInstance().getLogger().log(e);
		}
	}

	private void handleGiveEngagementGift(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);
		final ItemStack dropStack = LogicExtension.getGiftStackFromRelationship(player, entity);

		entity.entityDropItem(dropStack, 0.2F);
	}

	private void handleGiveAid(Object[] arguments, EntityPlayer player)
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);
		final ItemStack dropStack = null;

		Object[] giftInfo = null;
		giftInfo = 
				entity.profession == 0 ? Constants.farmerAidIDs[entity.worldObj.rand.nextInt(Constants.farmerAidIDs.length)] : 
					entity.profession == 4 ? Constants.butcherAidIDs[entity.worldObj.rand.nextInt(Constants.butcherAidIDs.length)] :
						Constants.bakerAidIDs[entity.worldObj.rand.nextInt(Constants.bakerAidIDs.length)];

					int quantityGiven = entity.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());

					entity.entityDropItem(new ItemStack((Item)giftInfo[0], quantityGiven), 0.2F);
	}

	private void handleUpdateFurnace(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

		BlockFurnace.updateFurnaceBlockState((Boolean)arguments[1], entity.worldObj, entity.cookingChore.furnacePosX, entity.cookingChore.furnacePosY, entity.cookingChore.furnacePosZ);
	}

	private void handleSyncRequest(Object[] arguments, EntityPlayer player) 
	{
		final int requestedId = (Integer) arguments[0];

		for (final World world : MinecraftServer.getServer().worldServers)
		{
			for (final Object obj : world.loadedEntityList)
			{
				if (obj instanceof AbstractEntity)
				{
					AbstractEntity entity = (AbstractEntity)obj;

					if (entity.getEntityId() == requestedId)
					{
						MCA.packetPipeline.sendPacketToPlayer(new Packet(EnumPacketType.Sync, entity.getEntityId(), entity), (EntityPlayerMP) player);
						MCA.packetPipeline.sendPacketToPlayer(new Packet(EnumPacketType.SetInventory, entity.getEntityId(), entity.inventory), (EntityPlayerMP) player); 
						MCA.getInstance().entitiesMap.put(entity.mcaID, entity);
						break;
					}
				}
			}
		}
	}

	private void handleSync(Object[] arguments, EntityPlayer player)
	{
		final int receivedId = (Integer)arguments[0];
		final AbstractEntity receivedEntity = (AbstractEntity)arguments[1];
		final AbstractEntity clientEntity = (AbstractEntity)player.worldObj.getEntityByID(receivedId);

		if (clientEntity == null)
		{
			MCA.getInstance().getLogger().log("WARNING: Sync failure. Entity ID not found on client - " + receivedId);
		}

		else
		{
			//Figure out which classes the entity is composed of.
			List<Class> classList = new ArrayList<Class>();
			classList.add(AbstractEntity.class);
			classList.add(receivedEntity.getClass());

			if (receivedEntity instanceof EntityPlayerChild || receivedEntity instanceof EntityVillagerChild)
			{
				classList.add(AbstractChild.class);
			}

			for (final Class clazz : classList)
			{
				for (final Field field : clazz.getDeclaredFields())
				{
					try
					{
						//Assign each chore an owner.
						if (field.get(receivedEntity) instanceof AbstractChore)
						{
							AbstractChore theChore = (AbstractChore)field.get(receivedEntity);
							theChore.owner = clientEntity;

							field.set(clientEntity, theChore);
						}

						//Assign the family tree an owner.
						else if (field.get(receivedEntity) instanceof FamilyTree)
						{
							FamilyTree theFamilyTree = (FamilyTree)field.get(receivedEntity);
							theFamilyTree.owner = clientEntity;

							field.set(clientEntity, theFamilyTree);
						}

						//Assign the inventory an owner.
						else if (field.get(receivedEntity) instanceof Inventory)
						{
							Inventory theInventory = (Inventory)field.get(receivedEntity);
							theInventory.owner = clientEntity;

							if (!clientEntity.inventory.equals(theInventory))
							{
								field.set(clientEntity, theInventory);
							}
						}

						else if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
						{
							field.set(clientEntity, field.get(receivedEntity));
						}
					}

					catch (IllegalAccessException e)
					{
						continue;
					}
				}
			}

			clientEntity.addAI();
			clientEntity.setTexture(receivedEntity.getTexture());
			clientEntity.setMoodByMoodPoints(false);
			clientEntity.trait = EnumTrait.getTraitById(clientEntity.traitId);

			MCA.getInstance().idsMap.put(clientEntity.mcaID, receivedId);
			MCA.getInstance().entitiesMap.put(clientEntity.mcaID, clientEntity);

			return;
		}
	}

	private void handleSyncEditorSettings(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

		entity.name = (String)arguments[1];
		entity.isMale = (Boolean)arguments[2];
		entity.profession = (Integer)arguments[3];
		entity.moodPointsAnger = (Float)arguments[4];
		entity.moodPointsHappy = (Float)arguments[5];
		entity.moodPointsSad = (Float)arguments[6];
		entity.traitId = (Integer)arguments[7];
		entity.inventory = (Inventory)arguments[8];
		entity.texture = (String)arguments[9];

		mod.getPacketPipeline().sendPacketToAllPlayers(new Packet(EnumPacketType.Sync, entity.getEntityId(), entity));
	}

	private void handleSwingArm(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

		entity.swingItem();
	}

	private void handleStopJumping(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

		entity.setJumping(false);
	}

	private void handleStartTrade(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final EntityVillagerAdult villager = (EntityVillagerAdult)player.worldObj.getEntityByID(entityId);

		villager.setCustomer(player);
		player.displayGUIMerchant(villager, villager.getTitle(MCA.getInstance().getIdOfPlayer(player), true));
	}

	private void handleSetWorldProperties(Object[] arguments, EntityPlayer player) 
	{
		final WorldPropertiesManager recvManager = (WorldPropertiesManager) arguments[0];
		final WorldPropertiesManager myManager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());

		try
		{
			if (myManager != null)
			{
				if (player.worldObj.isRemote) //Received from the server.
				{
					for (final Field field : WorldPropertiesList.class.getDeclaredFields())
					{
						final Object serverValue = field.get(recvManager.worldProperties);
						final Object clientValue = field.get(myManager.worldProperties);

						if (!clientValue.equals(serverValue))
						{
							field.set(myManager.worldProperties, serverValue);
							
							if (MCA.getInstance().inDebugMode)
							{
								MCA.getInstance().getLogger().log("Updated field: " + field.getName() + " : " + serverValue);
							}
						}
					}
				}

				else //Received from client.
				{
					for (final Field field : WorldPropertiesList.class.getDeclaredFields())
					{
						final Object clientValue = field.get(recvManager.worldProperties);
						final Object serverValue = field.get(myManager.worldProperties);

						if (MCA.getInstance().inDebugMode)
						{
							MCA.getInstance().getLogger().log(field.getName() + ":" + clientValue + ":" + serverValue);
						}
						
						if (!serverValue.equals(clientValue) && !field.getName().equals("playerID"))
						{
							field.set(myManager.worldProperties, clientValue);
							MCA.getInstance().getLogger().log("Updated field: " + field.getName() + " : " + clientValue);
						}
					}
					
					myManager.saveWorldProperties();
				}
			}
			
			else
			{
				MCA.getInstance().playerWorldManagerMap.put(player.getCommandSenderName(), recvManager);
			}
		}

		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	private void handleSetTombstoneText(Object[] arguments, EntityPlayer player) 
	{
		final int xCoord = (Integer) arguments[0];
		final int yCoord = (Integer) arguments[1];
		final int zCoord = (Integer) arguments[2];
		final String line1 = (String) arguments[3];
		final String line2 = (String) arguments[4];
		final String line3 = (String) arguments[5];
		final String line4 = (String) arguments[6];

		final TileEntityTombstone tombstone = (TileEntityTombstone)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		tombstone.signText[0] = line1;
		tombstone.signText[1] = line2;
		tombstone.signText[2] = line3;
		tombstone.signText[3] = line4;
		tombstone.markDirty();
	}

	private void handleSetTarget(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final int targetId = (Integer)arguments[1];

		for (final Object obj : player.worldObj.loadedEntityList)
		{
			Entity entity = (Entity)obj;

			if (entity.getEntityId() == entityId)
			{
				AbstractEntity clientEntity = (AbstractEntity)entity;

				if (targetId == 0)
				{
					clientEntity.target = null;
				}

				else
				{
					clientEntity.target = (EntityLivingBase)clientEntity.worldObj.getEntityByID(targetId);
				}
			}
		}
	}

	private void handleSetPosition(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final double xCoord = (Double)arguments[1];
		final double yCoord = (Double)arguments[2];
		final double zCoord = (Double)arguments[3];

		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);
		entity.setPosition(xCoord, yCoord, zCoord);
	}

	private void handleSetInventory(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final Inventory inventory = (Inventory)arguments[1];

		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(entityId);
		inventory.owner = entity;
		entity.inventory = inventory;
		entity.inventory.setWornArmorItems();

		if (!player.worldObj.isRemote)
		{
			mod.getPacketPipeline().sendPacketToAllPlayersExcept(new Packet(EnumPacketType.SetInventory, entityId, inventory), (EntityPlayerMP)player);
		}
	}

	private void handleSetFieldValue(Object[] arguments, EntityPlayer player) 
	{
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		final int entityId = (Integer) arguments[0];
		final String fieldName = (String) arguments[1];
		final Object fieldValue = arguments[2];

		if (MCA.getInstance().debugDoLogPackets && MCA.getInstance().inDebugMode)
		{
			MCA.getInstance().getLogger().log("\t" + entityId + " | " + fieldName + " | " + fieldValue.toString());
		}

		for (Object obj : player.worldObj.loadedEntityList)
		{
			try
			{
				final Entity entity = (Entity)obj;

				if (entity.getEntityId() == entityId)
				{
					final AbstractEntity abstractEntity = (AbstractEntity)entity;

					for (final Field f : entity.getClass().getFields())
					{
						if (!fieldName.equals("texture"))
						{
							if (f.getName().equals(fieldName))
							{
								//Achievements
								if (f.getName().equals("isPeasant") && fieldValue.toString().equals("true"))
								{
									manager.worldProperties.stat_villagersMadePeasants++;
									player.triggerAchievement(MCA.getInstance().achievementMakePeasant);

									if (manager.worldProperties.stat_villagersMadePeasants >= 20)
									{
										player.triggerAchievement(MCA.getInstance().achievementPeasantArmy);
									}

									manager.saveWorldProperties();
								}

								if (f.getName().equals("isKnight") && fieldValue.toString().equals("true"))
								{
									manager.worldProperties.stat_guardsMadeKnights++;
									player.triggerAchievement(MCA.getInstance().achievementMakeKnight);

									if (manager.worldProperties.stat_guardsMadeKnights >= 20)
									{
										player.triggerAchievement(MCA.getInstance().achievementKnightArmy);
									}

									manager.saveWorldProperties();
								}

								if (f.getName().equals("hasBeenExecuted") && fieldValue.toString().equals("true"))
								{
									manager.worldProperties.stat_villagersExecuted++;
									player.triggerAchievement(MCA.getInstance().achievementExecuteVillager);

									if (abstractEntity.familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) == EnumRelation.Spouse)
									{
										manager.worldProperties.stat_wivesExecuted++;

										if (manager.worldProperties.stat_wivesExecuted >= 6)
										{
											player.triggerAchievement(MCA.getInstance().achievementMonarchSecret);
										}
									}
									manager.saveWorldProperties();

								}
								//Setting the value.
								if (f.getType().getName().contains("boolean"))
								{
									entity.getClass().getField(fieldName).set(entity, Boolean.parseBoolean(fieldValue.toString()));

									//Special condition. When isSpouse is changed, a villager's AI must be updated just in case it is a guard who is
									//either getting married or getting divorced.
									if (f.getName().equals("isSpouse"))
									{
										abstractEntity.addAI();
									}
								}

								else if (f.getType().getName().contains("int"))
								{
									entity.getClass().getField(fieldName).set(entity, Integer.parseInt(fieldValue.toString()));

									if (f.getName().equals("traitId"))
									{
										abstractEntity.trait = EnumTrait.getTraitById(abstractEntity.traitId);
									}

									if (f.getName().equals("profession"))
									{
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
										PlayerMemory memory = memoryMap.get(player.getCommandSenderName());

										if (memory != null)
										{
											memory.playerName = player.getCommandSenderName();
											memoryMap.put(player.getCommandSenderName(), memory);
										}

										else
										{
											memoryMap.put(player.getCommandSenderName(), new PlayerMemory(player.getCommandSenderName()));
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

			catch (Throwable e)
			{
				MCA.getInstance().getLogger().log("Error setting field value.");
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}

		//Sync with all other players if server side.
		if (!player.worldObj.isRemote)
		{
			MCA.packetPipeline.sendPacketToAllPlayersExcept(new Packet(EnumPacketType.SetFieldValue, entityId, fieldName, fieldValue), (EntityPlayerMP) player);
		}
	}

	private void handleSetFamilyTree(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final FamilyTree familyTree = (FamilyTree)arguments[1];

		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(entityId);
		familyTree.owner = entity;
		entity.familyTree = familyTree;
	}

	private void handleSetChore(Object[] arguments, EntityPlayer player) 
	{
		//Assign received data.
		final int entityId = (Integer)arguments[0];
		final AbstractChore chore = (AbstractChore)arguments[1];

		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(entityId);
		chore.owner = entity;

		if (chore instanceof ChoreFarming)
		{
			entity.farmingChore = (ChoreFarming) chore;
			entity.farmingChore.cropEntry = ChoreRegistry.getFarmingCropEntries().get(entity.farmingChore.entryIndex);
		}

		else if (chore instanceof ChoreWoodcutting)
		{
			entity.woodcuttingChore = (ChoreWoodcutting) chore;
			entity.woodcuttingChore.treeEntry = ChoreRegistry.getWoodcuttingTreeEntries().get(entity.woodcuttingChore.treeTypeIndex);
		}

		else if (chore instanceof ChoreFishing)
		{
			entity.fishingChore = (ChoreFishing) chore;
		}

		else if (chore instanceof ChoreMining)
		{
			entity.miningChore = (ChoreMining) chore;
			entity.miningChore.oreEntry = ChoreRegistry.getMiningOreEntries().get(entity.miningChore.entryIndex);
			entity.miningChore.searchBlock = entity.miningChore.oreEntry.getOreBlock();
		}

		else if (chore instanceof ChoreCombat)
		{
			entity.combatChore = (ChoreCombat) chore;
		}

		else if (chore instanceof ChoreHunting)
		{
			entity.huntingChore = (ChoreHunting) chore;
		}

		else if (chore instanceof ChoreCooking)
		{
			entity.cookingChore = (ChoreCooking) chore;
		}

		else
		{
			MCA.getInstance().getLogger().log("Unidentified chore type received when handling chore packet.");
		}
	}

	private void handleSayLocalized(Object[] arguments, EntityPlayer player) 
	{
		boolean hasPlayer = (Boolean) arguments[0];
		boolean hasEntity = (Boolean) arguments[1];
		boolean hasPrefix = (Boolean) arguments[2];
		boolean hasSuffix = (Boolean) arguments[3];
		final String playerName = (String) arguments[4];
		final int entityId = (Integer) arguments[5];
		final String phraseId = (String) arguments[6];
		final boolean useCharacterType = (Boolean) arguments[7];
		final String prefix = (String) arguments[8];
		final String suffix = (String) arguments[9];

		EntityPlayer receivedPlayer = null;
		AbstractEntity entity = null;

		if (hasPlayer)
		{
			receivedPlayer = player.worldObj.getPlayerEntityByName(playerName);
		}

		if (hasEntity)
		{
			entity = (AbstractEntity)player.worldObj.getEntityByID(entityId);
		}

		if (entityId != -1)
		{
			if (receivedPlayer != null)
			{
				entity.lastInteractingPlayer = receivedPlayer.getCommandSenderName();
				entity.say(MCA.getInstance().getLanguageLoader().getString(phraseId, receivedPlayer, entity, useCharacterType, prefix, suffix));
			}

			else
			{
				entity.lastInteractingPlayer = player.getCommandSenderName();
				entity.say(MCA.getInstance().getLanguageLoader().getString(phraseId, player, entity, useCharacterType, prefix, suffix));
			}
		}

		//There isn't a speaker, so just add the localized string to the player's chat log.
		else
		{
			if (receivedPlayer != null)
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(phraseId, receivedPlayer, null, useCharacterType, prefix, suffix)));
			}

			else
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(phraseId, player, null, useCharacterType, prefix, suffix)));
			}
		}
	}

	private void handleReturnInventory(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer) arguments[0];
		final AbstractEntity entity = (AbstractEntity)player.worldObj.getEntityByID(entityId);

		ArrayList<EntityItem> itemList = MCA.getInstance().deadPlayerInventories.get(player.getCommandSenderName());

		for (EntityItem item : itemList)
		{
			entity.entityDropItem(item.getEntityItem(), 0.3F);
		}

		MCA.getInstance().deadPlayerInventories.remove(player.getCommandSenderName());
	}

	private void handleRemoveItem(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer) arguments[0];
		final int slot = (Integer) arguments[1];
		final int amount = (Integer) arguments[2];
		final int damage = (Integer) arguments[3];

		final EntityPlayer entityPlayer = (EntityPlayer) player.worldObj.getEntityByID(entityId);
		player.inventory.decrStackSize(slot, amount);
	}

	private void handlePlayerMarriage(Object[] arguments, EntityPlayer player)
	{
		final int playerId = (Integer)arguments[0];
		final String playerName = (String)arguments[1];
		final int spouseId = (Integer)arguments[2];

		//Workaround for problems on a server.
		String displayString = MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.marry.accept", player, null, false, "\u00a7A", null);
		displayString = displayString.replace("%SpouseName%", playerName);

		player.addChatMessage(new ChatComponentText(displayString));
		player.inventory.consumeInventoryItem(MCA.getInstance().itemWeddingRing);
	}

	private void handleOpenGui(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer) arguments[0];
		final byte guiId = (Byte) arguments[1];
		final Entity entity = player.worldObj.getEntityByID(entityId);

		if (guiId == Constants.ID_GUI_SETUP && MCA.getInstance().hasReceivedClientSetup)
		{
			return;
		}

		else if (guiId == Constants.ID_GUI_SETUP && !MCA.getInstance().hasReceivedClientSetup)
		{
			MCA.getInstance().hasReceivedClientSetup = true;
		}

		player.openGui(MCA.getInstance(), guiId, player.worldObj, (int)entity.posX, (int)entity.posY, (int)entity.posZ);
	}

	private void handleNotifyPlayer(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		AbstractEntity entity = null;

		try
		{
			entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

			if (entity == null)
			{
				int mcaId = -1;

				for (Map.Entry<Integer, Integer> entry : MCA.getInstance().idsMap.entrySet())
				{
					if (entry.getValue() == entityId)
					{
						mcaId = entry.getKey();
						break;
					}
				}

				if (mcaId > -1)
				{
					entity = MCA.getInstance().entitiesMap.get(mcaId);
				}
			}
		}

		catch (ClassCastException e) 
		{ 
			//Occurs when player passed as an argument.
		}

		final String phraseId = (String)arguments[1];
		player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(phraseId, null, entity, false)));
	}

	private void handleNameBaby(Object[] arguments, EntityPlayer player) 
	{
		final int villagerId = (Integer)arguments[0];
		final boolean babyIsMale = (Boolean)arguments[1];

		AbstractEntity villager = (AbstractEntity)player.worldObj.getEntityByID(villagerId);
		AbstractBaby itemBaby = null;

		//Unlock the appropriate achievement.
		if (babyIsMale)
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyBoy;
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);
		}

		else
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyGirl;
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyGirl);
		}

		//Give the baby to the villager.
		villager.inventory.addItemStackToInventory(new ItemStack(itemBaby, 1));
		mod.getPacketPipeline().sendPacketToServer(new Packet(EnumPacketType.SetInventory, villagerId, villager.inventory));

		//Modify the player's world properties manager.
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		manager.worldProperties.babyIsMale = babyIsMale;
		manager.worldProperties.babyExists = true;
		manager.saveWorldProperties();

		//Make the player choose a name for the baby.
		player.openGui(MCA.getInstance(), Constants.ID_GUI_NAMECHILD, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
	}

	private void handleMountHorse(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final int horseId = (Integer)arguments[1];

		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);
		final EntityHorse horse = (EntityHorse) player.worldObj.getEntityByID(horseId);

		if (horse.riddenByEntity != null && horse.riddenByEntity.getEntityId() == entity.getEntityId())
		{
			entity.dismountEntity(horse);
			entity.ridingEntity = null;
			horse.riddenByEntity = null;
			horse.setHorseSaddled(true);
		}

		else
		{
			if (horse.isTame() && horse.isAdultHorse() && horse.riddenByEntity == null && horse.isHorseSaddled())
			{
				entity.mountEntity(horse);
				horse.setHorseSaddled(false);
			}

			else
			{
				if (!entity.worldObj.isRemote)
				{
					entity.say(MCA.getInstance().getLanguageLoader().getString("notify.horse.invalid", player, entity, false));
				}
			}
		}

		if (!entity.worldObj.isRemote)
		{
			mod.getPacketPipeline().sendPacketToAllPlayers(new Packet(EnumPacketType.MountHorse, entityId, horseId));
		}
	}

	private void handleKillEntity(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);	

		entity.setDeadWithoutNotification();
	}

	private void handleHaveBaby(Object[] arguments, EntityPlayer player) 
	{
		final int playerId = (Integer) arguments[0];
		final int spouseId = (Integer) arguments[1];
		final EntityPlayer spouse = (EntityPlayer)player.worldObj.getEntityByID(spouseId);

		//Trigger the name baby gui.
		AbstractBaby itemBaby = null;
		boolean babyIsMale = Utility.getRandomGender();

		if (babyIsMale)
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyBoy;
		}

		else
		{
			itemBaby = (AbstractBaby)MCA.getInstance().itemBabyGirl;
		}

		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		manager.worldProperties.babyIsMale = babyIsMale;
		manager.worldProperties.babyExists = true;
		manager.saveWorldProperties();

		mod.getPacketPipeline().sendPacketToServer(new Packet(EnumPacketType.AddBaby, babyIsMale));
		player.openGui(MCA.getInstance(), Constants.ID_GUI_NAMECHILD, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
	}

	private void handleGetTombstoneText(Object[] arguments, EntityPlayer player) 
	{
		final int xCoord = (Integer) arguments[0];
		final int yCoord = (Integer) arguments[1];
		final int zCoord = (Integer) arguments[2];

		final TileEntityTombstone tombstone = (TileEntityTombstone)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		MCA.packetPipeline.sendPacketToPlayer(new Packet(EnumPacketType.SetTombstoneText, xCoord, yCoord, zCoord, 
				tombstone.signText[0], tombstone.signText[1], tombstone.signText[2], tombstone.signText[3]), (EntityPlayerMP)player);
	}

	private void handleForceRespawn(Object[] arguments, EntityPlayer player) 
	{
		final MinecraftServer theServer = MinecraftServer.getServer();
		final EntityPlayerMP playerMP = (EntityPlayerMP) player;

		theServer.getConfigurationManager().respawnPlayer(playerMP, 0, false);
	}

	private void handleEngagement(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer) arguments[0];

		List<Entity> entitiesAroundMe = LogicHelper.getAllEntitiesWithinDistanceOfEntity(player.worldObj.getEntityByID(entityId), 64);

		for (Entity entity : entitiesAroundMe)
		{
			if (entity instanceof EntityVillagerAdult)
			{
				EntityVillagerAdult entityVillager = (EntityVillagerAdult)entity;

				if (entityVillager.playerMemoryMap.containsKey(player.getCommandSenderName()))
				{
					PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
					memory.hasGift = true;
					entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);
				}
			}
		}
	}

	private void handleDropItem(Object[] arguments, EntityPlayer player) 
	{
		// TODO Auto-generated method stub
	}

	private void handleClientSideCommand(Object[] arguments, EntityPlayer player) 
	{
		final String commandName = arguments[0].toString();
		final ICommandSender sender = (ICommandSender)player;

		MinecraftServer.getServer().getCommandManager().executeCommand(sender, commandName);
	}

	private void handleRemoveMarriageRequest(Object[] arguments, EntityPlayer player) 
	{
		final String senderName = arguments[0].toString();
		MCA.getInstance().marriageRequests.remove(senderName);
	}

	private void handleRemoveBabyRequest(Object[] arguments, EntityPlayer player) 
	{
		final String senderName = arguments[0].toString();
		MCA.getInstance().babyRequests.remove(senderName);
	}

	private void handleAddMarriageRequest(Object[] arguments, EntityPlayer player) 
	{
		final String senderName = arguments[0].toString();
		final String recipientName = arguments[1].toString();

		MCA.getInstance().marriageRequests.put(senderName, recipientName);
	}

	private void handleAddBabyRequest(Object[] arguments, EntityPlayer player) 
	{
		final String senderName = arguments[0].toString();
		final String recipientName = arguments[1].toString();

		MCA.getInstance().babyRequests.put(senderName, recipientName);
	}

	private void handleBroadcastKillEntity(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer)arguments[0];
		MCA.packetPipeline.sendPacketToAllPlayersExcept(new Packet(EnumPacketType.KillEntity, entityId), (EntityPlayerMP)player);
	}

	private void handleBabyInfo(Object[] arguments, EntityPlayer player) 
	{
		final WorldPropertiesManager receivedManager = (WorldPropertiesManager) arguments[0];

		//Set the player's spouse's manager to have the same baby info.
		WorldPropertiesManager spouseManager = MCA.getInstance().playerWorldManagerMap.get(receivedManager.worldProperties.playerSpouseName);

		spouseManager.worldProperties.babyExists = receivedManager.worldProperties.babyExists;
		spouseManager.worldProperties.babyIsMale = receivedManager.worldProperties.babyIsMale;
		spouseManager.worldProperties.babyName = receivedManager.worldProperties.babyName;
		spouseManager.worldProperties.babyReadyToGrow = receivedManager.worldProperties.babyReadyToGrow;

		spouseManager.saveWorldProperties();
	}

	private void handleArrangedMarriageParticles(Object[] arguments, EntityPlayer player) 
	{
		final int entityId1 = (Integer)arguments[0];
		final int entityId2 = (Integer)arguments[1];
		final AbstractEntity entity1 = (AbstractEntity) player.worldObj.getEntityByID(entityId1);
		final AbstractEntity entity2 = (AbstractEntity) player.worldObj.getEntityByID(entityId2);

		for (int loops = 0; loops < 16; loops++)
		{
			final double velX = MCA.rand.nextGaussian() * 0.02D;
			final double velY = MCA.rand.nextGaussian() * 0.02D;
			final double velZ = MCA.rand.nextGaussian() * 0.02D;

			entity1.worldObj.spawnParticle("happyVillager", entity1.posX + MCA.rand.nextFloat() * entity1.width * 2.0F - entity1.width, entity1.posY + 0.5D + MCA.rand.nextFloat() * entity1.height, entity1.posZ + MCA.rand.nextFloat() * entity1.width * 2.0F - entity1.width, velX, velY, velZ);
			entity2.worldObj.spawnParticle("happyVillager", entity2.posX + MCA.rand.nextFloat() * entity2.width * 2.0F - entity2.width, entity2.posY + 0.5D + MCA.rand.nextFloat() * entity2.height, entity2.posZ + MCA.rand.nextFloat() * entity2.width * 2.0F - entity2.width, velX, velY, velZ);
		}
	}

	private void handleAddBaby(Object[] arguments, EntityPlayer player) 
	{
		final Item itemToAdd = ((Boolean)arguments[0]) == true ? MCA.getInstance().itemBabyBoy : MCA.getInstance().itemBabyGirl;
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		EntityPlayer spousePlayer = null;

		//Check for spouse.
		if (manager != null && manager.worldProperties.playerSpouseID < 0)
		{
			spousePlayer = player.worldObj.getPlayerEntityByName(manager.worldProperties.playerSpouseName);
		}

		player.inventory.addItemStackToInventory(new ItemStack(itemToAdd));

		if (itemToAdd instanceof ItemBabyBoy)
		{
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);

			if (spousePlayer != null)
			{
				spousePlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);	
			}
		}

		else if (itemToAdd instanceof ItemBabyGirl)
		{
			player.triggerAchievement(MCA.getInstance().achievementHaveBabyBoy);

			if (spousePlayer != null)
			{
				spousePlayer.triggerAchievement(MCA.getInstance().achievementHaveBabyGirl);
			}
		}
	}

	private void handleAddAI(Object[] arguments, EntityPlayer player) 
	{
		final int entityId = (Integer) arguments[0];
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(entityId);

		entity.addAI();
	}
}

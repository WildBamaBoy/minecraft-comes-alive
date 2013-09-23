/*******************************************************************************
 * PacketHelper.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import mca.chore.AbstractChore;
import mca.core.MCA;
import mca.core.io.ModPropertiesManager;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.object.FamilyTree;
import mca.entity.AbstractEntity;
import mca.entity.EntityVillagerAdult;
import mca.inventory.Inventory;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.stats.Achievement;

/**
 * Creates packets used by MCA to communicate between the client and server.
 */
public final class PacketHelper 
{
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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

			MCA.instance.logPacketInformation("Created world properties packet for " + worldPropertiesManager.worldProperties.playerName);
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
	}

	/**
	 * Creates a packet used to make a villager who is procreating with a player actually have a baby.
	 * 
	 * @param	villager	The villager that is procreating with the player.
	 * @param	player		The player that is procreating with the villager.
	 * @param	babyGender	The gender of the baby that will be added to the villager's inventory.
	 * 
	 * @return	A VillagerPlayerProcreate packet.
	 */
	public static Packet createVillagerPlayerProcreatePacket(AbstractEntity villager, EntityPlayer player, String babyGender) 
	{
		try
		{
			Packet250CustomPayload thePacket = new Packet250CustomPayload();
			thePacket.channel = "MCA_VPPROC";
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			
			objectOutput.writeObject(villager.entityId);
			objectOutput.writeObject(player.entityId);
			objectOutput.writeObject(babyGender);
			objectOutput.close();
			
			thePacket.data = MCA.compressBytes(byteOutput.toByteArray());
			thePacket.length = thePacket.data.length;
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
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
			
			MCA.instance.logPacketInformation("Sent packet: " + thePacket.channel);
			return thePacket;
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
			return null;
		}
	}
}
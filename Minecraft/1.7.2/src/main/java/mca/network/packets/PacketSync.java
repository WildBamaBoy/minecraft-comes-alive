package mca.network.packets;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import mca.chore.AbstractChore;
import mca.core.MCA;
import mca.core.util.object.FamilyTree;
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerChild;
import mca.enums.EnumTrait;
import mca.inventory.Inventory;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSync extends AbstractPacket implements IMessage, IMessageHandler<PacketSync, IMessage>
{
	private int entityId;
	private AbstractEntity entity;

	public PacketSync()
	{
	}

	public PacketSync(int entityId, AbstractEntity entity)
	{
		this.entityId = entityId;
		this.entity = entity;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		entity = (AbstractEntity)ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, entity);
	}

	@Override
	public IMessage onMessage(PacketSync packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity clientEntity = (AbstractEntity)player.worldObj.getEntityByID(packet.entityId);

		if (clientEntity == null)
		{
			MCA.getInstance().getLogger().log("WARNING: Sync failure. Entity ID not found on client - " + packet.entityId);
		}

		else
		{
			//Figure out which classes the entity is composed of.
			List<Class> classList = new ArrayList<Class>();
			classList.add(AbstractEntity.class);
			classList.add(packet.entity.getClass());

			if (packet.entity instanceof EntityPlayerChild || packet.entity instanceof EntityVillagerChild)
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
						if (field.get(packet.entity) instanceof AbstractChore)
						{
							AbstractChore theChore = (AbstractChore)field.get(packet.entity);
							theChore.owner = clientEntity;

							field.set(clientEntity, theChore);
						}

						//Assign the family tree an owner.
						else if (field.get(packet.entity) instanceof FamilyTree)
						{
							FamilyTree theFamilyTree = (FamilyTree)field.get(packet.entity);
							theFamilyTree.owner = clientEntity;

							field.set(clientEntity, theFamilyTree);
						}

						//Assign the inventory an owner.
						else if (field.get(packet.entity) instanceof Inventory)
						{
							Inventory theInventory = (Inventory)field.get(packet.entity);
							theInventory.owner = clientEntity;

							if (!clientEntity.inventory.equals(theInventory))
							{
								field.set(clientEntity, theInventory);
							}
						}

						else if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
						{
							field.set(clientEntity, field.get(packet.entity));
						}
					}

					catch (IllegalAccessException e)
					{
						continue;
					}
				}
			}

			clientEntity.addAI();
			clientEntity.setTexture(packet.entity.getTexture());
			clientEntity.setMoodByMoodPoints(false);
			clientEntity.trait = EnumTrait.getTraitById(clientEntity.traitId);

			MCA.getInstance().idsMap.put(clientEntity.mcaID, packet.entityId);
			MCA.getInstance().entitiesMap.put(clientEntity.mcaID, clientEntity);
		}

		return null;
	}
}

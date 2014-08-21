package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.inventory.Inventory;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncEditorSettings extends AbstractPacket implements IMessage, IMessageHandler<PacketSyncEditorSettings, IMessage>
{
	private int entityId;
	private String name;
	private boolean isMale;
	private int profession;
	private float moodPointsAnger;
	private float moodPointsHappy;
	private float moodPointsSad;
	private int traitId;
	private Inventory inventory;
	private String texture;

	public PacketSyncEditorSettings()
	{
	}

	public PacketSyncEditorSettings(AbstractEntity entity)
	{
		this.entityId = entity.getEntityId();
		this.name = entity.name;
		this.isMale = entity.isMale;
		this.profession = entity.profession;
		this.moodPointsAnger = entity.moodPointsAnger;
		this.moodPointsHappy = entity.moodPointsHappy;
		this.moodPointsSad = entity.moodPointsSad;
		this.traitId = entity.traitId;
		this.inventory = entity.inventory;
		this.texture = entity.getTexture();
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		this.entityId = byteBuf.readInt();
		this.name = (String)ByteBufIO.readObject(byteBuf);
		this.isMale = byteBuf.readBoolean();
		this.profession = byteBuf.readInt();
		this.moodPointsAnger = byteBuf.readFloat();
		this.moodPointsHappy = byteBuf.readFloat();
		this.moodPointsSad = byteBuf.readFloat();
		this.traitId = byteBuf.readInt();
		this.inventory = (Inventory)ByteBufIO.readObject(byteBuf);
		this.texture = (String)ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, name);
		byteBuf.writeBoolean(isMale);
		byteBuf.writeInt(profession);
		byteBuf.writeFloat(moodPointsAnger);
		byteBuf.writeFloat(moodPointsHappy);
		byteBuf.writeFloat(moodPointsSad);
		byteBuf.writeInt(traitId);
		ByteBufIO.writeObject(byteBuf, inventory);
		ByteBufIO.writeObject(byteBuf, texture);
	}

	@Override
	public IMessage onMessage(PacketSyncEditorSettings packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final AbstractEntity entity = (AbstractEntity) player.worldObj.getEntityByID(packet.entityId);

		if (player != null && entity != null)
		{
			entity.name = packet.name;
			entity.isMale = packet.isMale;
			entity.profession = packet.profession;
			entity.moodPointsAnger = packet.moodPointsAnger;
			entity.moodPointsHappy = packet.moodPointsHappy;
			entity.moodPointsSad = packet.moodPointsSad;
			entity.traitId = packet.traitId;
			entity.inventory = packet.inventory;
			entity.texture = packet.texture;

			MCA.packetHandler.sendPacketToAllPlayers(new PacketSync(entity.getEntityId(), entity));
		}
		
		return null;
	}
}

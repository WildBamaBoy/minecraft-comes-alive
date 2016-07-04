package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerDataCollection;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

/* Player data change received from a player. */
public class PacketPlayerDataS extends AbstractPacket implements IMessage, IMessageHandler<PacketPlayerDataS, IMessage>
{
	private NBTPlayerData.FieldUpdateObj fieldUpdateObj;
	
	public PacketPlayerDataS()
	{
	}

	public PacketPlayerDataS(NBTPlayerData.FieldUpdateObj fieldUpdateObj)
	{
		this.fieldUpdateObj = fieldUpdateObj;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		int fieldId = byteBuf.readInt();
		int typeId = byteBuf.readInt();
		Object value = ByteBufIO.readObject(byteBuf);
		
		fieldUpdateObj = NBTPlayerData.FieldUpdateObj.get(NBTPlayerData.FieldID.fromId(fieldId), NBTPlayerData.TypeID.fromId(typeId), value);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(fieldUpdateObj.fieldId.getId());
		byteBuf.writeInt(fieldUpdateObj.typeId.getId());
		ByteBufIO.writeObject(byteBuf, fieldUpdateObj.value);
	}

	@Override
	public IMessage onMessage(PacketPlayerDataS packet, MessageContext context)
	{
		//Always mark for saving if data is being changed from the client.
		PlayerDataCollection.get().markDirty();
		
		NBTPlayerData data = MCA.getPlayerData(this.getPlayer(context));
		data.setByFieldUpdateObj(packet.fieldUpdateObj);
		
		System.out.println("DATA S " + packet.fieldUpdateObj.fieldId + " : " + packet.fieldUpdateObj.value);
		return null;
	}
}

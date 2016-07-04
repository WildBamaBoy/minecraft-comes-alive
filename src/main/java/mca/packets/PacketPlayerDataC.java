package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

/* Player data change received from the server. */
public class PacketPlayerDataC extends AbstractPacket implements IMessage, IMessageHandler<PacketPlayerDataC, IMessage>
{
	private NBTPlayerData.FieldUpdateObj fieldUpdateObj;
	
	public PacketPlayerDataC()
	{
	}

	public PacketPlayerDataC(NBTPlayerData.FieldUpdateObj fieldUpdateObj)
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
	public IMessage onMessage(PacketPlayerDataC packet, MessageContext context)
	{
		NBTPlayerData data = MCA.getPlayerData(this.getPlayerClient());
		data.setByFieldUpdateObj(packet.fieldUpdateObj);
		return null;
	}
}

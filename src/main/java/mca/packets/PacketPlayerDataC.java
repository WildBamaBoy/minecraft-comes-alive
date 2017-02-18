package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketPlayerDataC extends AbstractPacket<PacketPlayerDataC>
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
		Object value = RadixNettyIO.readObject(byteBuf);
		
		fieldUpdateObj = NBTPlayerData.FieldUpdateObj.get(NBTPlayerData.FieldID.fromId(fieldId), NBTPlayerData.TypeID.fromId(typeId), value);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(fieldUpdateObj.fieldId.getId());
		byteBuf.writeInt(fieldUpdateObj.typeId.getId());
		RadixNettyIO.writeObject(byteBuf, fieldUpdateObj.value);
	}

	@Override
	public void processOnGameThread(PacketPlayerDataC packet, MessageContext context) 
	{
		NBTPlayerData data = MCA.getPlayerData(this.getPlayerClient());
		data.setByFieldUpdateObj(packet.fieldUpdateObj);
	}
}

package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerDataCollection;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.RadixNettyIO;
import radixcore.modules.net.AbstractPacket;

public class PacketPlayerDataS extends AbstractPacket<PacketPlayerDataS>
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
	public void processOnGameThread(PacketPlayerDataS packet, MessageContext context) 
	{	
		//Always mark for saving if data is being changed from the client.
		PlayerDataCollection.get().markDirty();
		
		NBTPlayerData data = MCA.getPlayerData(this.getPlayer(context));
		data.setByFieldUpdateObj(packet.fieldUpdateObj);
	}
}

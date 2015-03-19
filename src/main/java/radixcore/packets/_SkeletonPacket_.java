package radixcore.packets;

import io.netty.buffer.ByteBuf;
import radixcore.network.ByteBufIO;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class _SkeletonPacket_ extends AbstractPacket implements IMessage, IMessageHandler<_SkeletonPacket_, IMessage>
{
	private int int1;
	private short short1;
	private String string1;

	public _SkeletonPacket_()
	{
		//Required
	}

	public _SkeletonPacket_(int int1, short short1, String string1)
	{
		this.int1 = int1;
		this.short1 = short1;
		this.string1 = string1;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		int1 = byteBuf.readInt();
		short1 = byteBuf.readShort();
		string1 = (String) ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(int1);
		byteBuf.writeShort(short1);
		ByteBufIO.writeObject(byteBuf, string1);
	}

	@Override
	public IMessage onMessage(_SkeletonPacket_ packet, MessageContext context)
	{
		//Qualify all variable access with packet.<field name>.
		return null;
	}
}

package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;

public class PacketPlaySoundOnPlayer extends AbstractPacket implements IMessage, IMessageHandler<PacketPlaySoundOnPlayer, IMessage>
{
	private String soundName;
	private float volume;
	private float pitch;
	
	public PacketPlaySoundOnPlayer()
	{
		//Required by Forge
	}
	
	public PacketPlaySoundOnPlayer(String soundName)
	{
		this.soundName = soundName;
		this.volume = 1.0F;
		this.pitch = 1.0F;
	}
	
	public PacketPlaySoundOnPlayer(String soundName, float volume, float pitch)
	{
		this(soundName);
		this.volume = volume;
		this.pitch = pitch;
	}
	
	@Override
	public void toBytes(ByteBuf buf) 
	{
		ByteBufIO.writeObject(buf, this.soundName);
		buf.writeFloat(this.volume);
		buf.writeFloat(this.pitch);		
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		this.soundName = (String) ByteBufIO.readObject(buf);		
		this.volume = buf.readFloat();
		this.pitch = buf.readFloat();
	}

	@Override
	public IMessage onMessage(PacketPlaySoundOnPlayer packet, MessageContext ctx) 
	{
		getPlayerClient().playSound(packet.soundName, packet.pitch, packet.volume);	
		return null;
	}
}

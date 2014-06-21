package mca.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketClientCommand extends AbstractPacket implements IMessage, IMessageHandler<PacketClientCommand, IMessage>
{
	private String command;

	public PacketClientCommand()
	{
	}
	
	public PacketClientCommand(String command)
	{
		this.command = command;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		command = (String)ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		ByteBufIO.writeObject(byteBuf, command);
	}

	@Override
	public IMessage onMessage(PacketClientCommand packet, MessageContext context) 
	{
		final ICommandSender sender = (ICommandSender)context.getServerHandler().playerEntity;
		MinecraftServer.getServer().getCommandManager().executeCommand(sender, packet.command);
		return null;
	}
}

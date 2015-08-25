package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;

public class PacketTombstoneUpdateSet extends AbstractPacket implements IMessage, IMessageHandler<PacketTombstoneUpdateSet, IMessage>
{
	private int x;
	private int y;
	private int z;
	private String[] text;

	public PacketTombstoneUpdateSet()
	{
	}

	public PacketTombstoneUpdateSet(TileTombstone tombstone)
	{
		this.x = tombstone.getPos().getX();
		this.y = tombstone.getPos().getY();
		this.z = tombstone.getPos().getZ();
		this.text = tombstone.signText;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{	
		text = new String[4];
		
		x = byteBuf.readInt();
		y = byteBuf.readInt();
		z = byteBuf.readInt();
		
		for (int i = 0; i < 4; i++)
		{
			String readText = (String) ByteBufIO.readObject(byteBuf);
			readText = readText.equals("@n/a@") ? "" : readText;
			
			text[i] = readText;
		}
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{	
		byteBuf.writeInt(x);
		byteBuf.writeInt(y);
		byteBuf.writeInt(z);
		
		for (String s : text)
		{
			if (s == null || s.isEmpty())
			{
				s = "@n/a@";
			}
			
			ByteBufIO.writeObject(byteBuf, s);
		}
	}

	@Override
	public IMessage onMessage(PacketTombstoneUpdateSet packet, MessageContext context)
	{
		MCA.getPacketHandler().addPacketForProcessing(context.side, packet, context);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		final PacketTombstoneUpdateSet packet = (PacketTombstoneUpdateSet)message;
		final EntityPlayer player = this.getPlayer(context);
		final World world = player.worldObj;
		
		try
		{
			final TileTombstone tombstone = (TileTombstone)BlockHelper.getTileEntity(world, packet.x, packet.y, packet.z);
			
			if (tombstone != null)
			{
				tombstone.signText = packet.text;
			}
			
			if (FMLCommonHandler.instance().getEffectiveSide().isServer())
			{
				MCA.getPacketHandler().sendPacketToAllPlayers(packet);
			}
		}
		
		catch (ClassCastException e)
		{
			//Throw away these exceptions.
		}
	}
}

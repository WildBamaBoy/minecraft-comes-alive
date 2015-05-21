package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

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
		this.x = tombstone.xCoord;
		this.y = tombstone.yCoord;
		this.z = tombstone.zCoord;
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
		
		return null;
	}
}

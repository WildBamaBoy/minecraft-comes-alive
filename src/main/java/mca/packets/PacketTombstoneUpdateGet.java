package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;

public class PacketTombstoneUpdateGet extends AbstractPacket implements IMessage, IMessageHandler<PacketTombstoneUpdateGet, IMessage>
{
	private int x;
	private int y;
	private int z;

	public PacketTombstoneUpdateGet()
	{
	}

	public PacketTombstoneUpdateGet(TileTombstone tombstone)
	{
		this.x = tombstone.xCoord;
		this.y = tombstone.yCoord;
		this.z = tombstone.zCoord;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{	
		x = byteBuf.readInt();
		y = byteBuf.readInt();
		z = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{	
		byteBuf.writeInt(x);
		byteBuf.writeInt(y);
		byteBuf.writeInt(z);
	}

	@Override
	public IMessage onMessage(PacketTombstoneUpdateGet packet, MessageContext context)
	{
		final EntityPlayer player = this.getPlayer(context);
		final World world = player.worldObj;
		
		try
		{
			final TileTombstone tombstone = (TileTombstone)BlockHelper.getTileEntity(world, packet.x, packet.y, packet.z);
			MCA.getPacketHandler().sendPacketToPlayer(new PacketTombstoneUpdateSet(tombstone), (EntityPlayerMP) player);
		}
		
		catch (ClassCastException e)
		{
			//Throw away these exceptions.
		}
		
		return null;
	}
}

package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.tile.TileMemorial;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;

public class PacketMemorialUpdateGet extends AbstractPacket implements IMessage, IMessageHandler<PacketMemorialUpdateGet, IMessage>
{
	private int x;
	private int y;
	private int z;

	public PacketMemorialUpdateGet()
	{
	}

	public PacketMemorialUpdateGet(TileMemorial memorial)
	{
		this.x = memorial.xCoord;
		this.y = memorial.yCoord;
		this.z = memorial.zCoord;
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
	public IMessage onMessage(PacketMemorialUpdateGet packet, MessageContext context)
	{
		final EntityPlayer player = this.getPlayer(context);
		final World world = player.worldObj;
		
		try
		{
			final TileMemorial memorial = (TileMemorial)BlockHelper.getTileEntity(world, packet.x, packet.y, packet.z);
			MCA.getPacketHandler().sendPacketToPlayer(new PacketMemorialUpdateSet(memorial), (EntityPlayerMP) player);
		}
		
		catch (ClassCastException e)
		{
			//Throw away these exceptions.
		}
		
		return null;
	}
}

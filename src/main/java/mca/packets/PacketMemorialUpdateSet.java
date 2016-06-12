package mca.packets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.enums.EnumMemorialType;
import mca.tile.TileMemorial;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import radixcore.network.ByteBufIO;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;

public class PacketMemorialUpdateSet extends AbstractPacket implements IMessage, IMessageHandler<PacketMemorialUpdateSet, IMessage>
{
	private int x;
	private int y;
	private int z;
	private int type;

	public PacketMemorialUpdateSet()
	{
	}

	public PacketMemorialUpdateSet(TileMemorial memorial)
	{
		this.x = memorial.xCoord;
		this.y = memorial.yCoord;
		this.z = memorial.zCoord;
		this.type = memorial.getType().getId();
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{	
		x = byteBuf.readInt();
		y = byteBuf.readInt();
		z = byteBuf.readInt();
		type = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{	
		byteBuf.writeInt(x);
		byteBuf.writeInt(y);
		byteBuf.writeInt(z);
		byteBuf.writeInt(type);
	}

	@Override
	public IMessage onMessage(PacketMemorialUpdateSet packet, MessageContext context)
	{
		final EntityPlayer player = this.getPlayer(context);
		final World world = player.worldObj;
		
		try
		{
			final TileMemorial memorial = (TileMemorial)BlockHelper.getTileEntity(world, packet.x, packet.y, packet.z);
			
			if (memorial != null)
			{
				memorial.setType(EnumMemorialType.fromId(packet.type));
			}
		}
		
		catch (ClassCastException e)
		{
			//Throw away these exceptions.
		}
		
		return null;
	}
}

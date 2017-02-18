package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.enums.EnumMemorialType;
import mca.tile.TileMemorial;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.net.AbstractPacket;

public class PacketMemorialUpdateSet extends AbstractPacket<PacketMemorialUpdateSet>
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
		this.x = memorial.getPos().getX();
		this.y = memorial.getPos().getY();
		this.z = memorial.getPos().getZ();
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
	public void processOnGameThread(PacketMemorialUpdateSet packet, MessageContext context) 
	{
		final EntityPlayer player = this.getPlayer(context);
		final World world = player.worldObj;
		
		try
		{
			final TileMemorial memorial = (TileMemorial)world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z));
			
			if (memorial != null)
			{
				memorial.setType(EnumMemorialType.fromId(packet.type));
			}
		}
		
		catch (ClassCastException e)
		{
			//Throw away these exceptions.
		}
	}
}

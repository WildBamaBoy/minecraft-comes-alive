package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.tile.TileMemorial;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.modules.net.AbstractPacket;

public class PacketMemorialUpdateGet extends AbstractPacket<PacketMemorialUpdateGet>
{
	private int x;
	private int y;
	private int z;

	public PacketMemorialUpdateGet()
	{
	}

	public PacketMemorialUpdateGet(TileMemorial memorial)
	{
		this.x = memorial.getPos().getX();
		this.y = memorial.getPos().getY();
		this.z = memorial.getPos().getZ();
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
	public void processOnGameThread(PacketMemorialUpdateGet packet, MessageContext context)
	{
		final EntityPlayer player = this.getPlayer(context);
		final World world = player.worldObj;
		
		try
		{
			final TileMemorial memorial = (TileMemorial)world.getTileEntity(new BlockPos(packet.x, packet.y, packet.z));
			
			if (memorial != null) 
			{
			    MCA.getPacketHandler().sendPacketToPlayer(new PacketMemorialUpdateSet(memorial), (EntityPlayerMP) player);
			}
		}
		
		catch (ClassCastException e)
		{
			//Throw away these exceptions.
		}
	}
}

package mca.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.math.Point3D;
import radixcore.modules.net.AbstractPacket;

public class PacketSpawnLightning extends AbstractPacket<PacketSpawnLightning>
{
	private Point3D position;
	
	public PacketSpawnLightning()
	{
		//Required by Forge
	}
	
	public PacketSpawnLightning(Point3D position)
	{
		this.position = position;
	}
	
	@Override
	public void toBytes(ByteBuf buf) 
	{
		buf.writeDouble(this.position.dX());
		buf.writeDouble(this.position.dY());
		buf.writeDouble(this.position.dZ());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		this.position = new Point3D(x, y, z);
	}

	@Override
	public void processOnGameThread(PacketSpawnLightning packet, MessageContext context) 
	{
		World world = getPlayerClient().worldObj;
		EntityLightningBolt lightning = new EntityLightningBolt(world, packet.position.dX(), packet.position.dY(), packet.position.dZ(), false);
		
		world.spawnEntityInWorld(lightning);
		getPlayerClient().playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 2.0F, 1.0F);
	}
}

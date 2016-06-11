package mca.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.World;
import radixcore.math.Point3D;
import radixcore.packets.AbstractPacket;

public class PacketSpawnLightning extends AbstractPacket implements IMessage, IMessageHandler<PacketSpawnLightning, IMessage>
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
		buf.writeDouble(this.position.dPosX);
		buf.writeDouble(this.position.dPosY);
		buf.writeDouble(this.position.dPosZ);
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
	public IMessage onMessage(PacketSpawnLightning packet, MessageContext ctx) 
	{
		World world = getPlayerClient().worldObj;
		EntityLightningBolt lightning = new EntityLightningBolt(world, packet.position.dPosX, packet.position.dPosY, packet.position.dPosZ);
		
		world.spawnEntityInWorld(lightning);
		getPlayerClient().playSound("ambient.weather.thunder", 2.0F, 1.0F);
		return null;
	}
}

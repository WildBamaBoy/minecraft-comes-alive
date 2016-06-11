package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
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
		MCA.getPacketHandler().addPacketForProcessing(ctx.side, packet, ctx);
		return null;
	}

	@Override
	public void processOnGameThread(IMessageHandler message, MessageContext context) 
	{
		PacketSpawnLightning packet = (PacketSpawnLightning)message;
		World world = getPlayerClient().worldObj;
		EntityLightningBolt lightning = new EntityLightningBolt(world, packet.position.dPosX, packet.position.dPosY, packet.position.dPosZ);
		
		world.spawnEntityInWorld(lightning);
		getPlayerClient().playSound("ambient.weather.thunder", 2.0F, 1.0F);
	}
}

package mca.packets;

import java.io.IOException;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.enums.EnumDestinyChoice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.chunk.Chunk;
import radixcore.data.BlockWithMeta;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;
import radixcore.math.Point3D;
import radixcore.packets.AbstractPacket;
import radixcore.util.SchematicReader;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDestinyChoice extends AbstractPacket implements IMessage, IMessageHandler<PacketDestinyChoice, IMessage>
{
	private int choice;

	public PacketDestinyChoice()
	{
	}

	public PacketDestinyChoice(EnumDestinyChoice choice)
	{
		this.choice = choice.getId();
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		choice = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(choice);
	}

	@Override
	public IMessage onMessage(PacketDestinyChoice packet, MessageContext context)
	{
		EntityPlayer senderPlayer = this.getPlayer(context);

		Chunk newSpawnChunk = null;
		int x, y, z = 0;

		do
		{
			x = (int) (MathHelper.getNumberInRange(-4096, 4096) + (senderPlayer.posX / 2));
			z = (int) (MathHelper.getNumberInRange(-4096, 4096) + (senderPlayer.posZ / 2));
			y = LogicHelper.getSpawnSafeTopLevel(senderPlayer.worldObj, x, z);

			newSpawnChunk = senderPlayer.worldObj.getChunkFromChunkCoords(x >> 4, z >> 4);
		}
		while (newSpawnChunk.getBiomeGenForWorldCoords(x & 15, z & 13, senderPlayer.worldObj.getWorldChunkManager()) instanceof BiomeGenOcean);

		senderPlayer.setPositionAndUpdate(x, y, z);

		return null;
	}
}

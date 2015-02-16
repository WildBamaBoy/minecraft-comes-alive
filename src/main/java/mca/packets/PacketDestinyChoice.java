package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.enums.EnumDestinyChoice;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;
import radixcore.math.Point3D;
import radixcore.packets.AbstractPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDestinyChoice extends AbstractPacket implements IMessage, IMessageHandler<PacketDestinyChoice, IMessage>
{
	private EnumDestinyChoice choice;

	public PacketDestinyChoice()
	{
	}

	public PacketDestinyChoice(EnumDestinyChoice choice)
	{
		this.choice = choice;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf)
	{
		choice = EnumDestinyChoice.fromId(byteBuf.readInt());
	}

	@Override
	public void toBytes(ByteBuf byteBuf)
	{
		byteBuf.writeInt(choice.getId());
	}

	@Override
	public IMessage onMessage(PacketDestinyChoice packet, MessageContext context)
	{
		final EntityPlayerMP senderPlayer = (EntityPlayerMP)this.getPlayer(context);
		final WorldServer world = (WorldServer)senderPlayer.worldObj;
		
		if (packet.choice == EnumDestinyChoice.NONE)
		{
			//Update the region around the player so that the destiny room disappears.
			final PlayerManager manager = world.getPlayerManager();
			
			for (int x = -20; x < 20; x++)
			{
				for (int z = -20; z < 20; z++)
				{
					for (int y = -5; y < 10; y++)
					{
						manager.markBlockForUpdate((int)senderPlayer.posX + x, (int)senderPlayer.posY + y, (int)senderPlayer.posZ + z);						
					}
				}
			}
		}

		else
		{
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

			final Point3D oldPoint = new Point3D(senderPlayer.posX, senderPlayer.posY, senderPlayer.posZ);
			senderPlayer.setPositionAndUpdate(x, y, z);
			
			//TODO Build village/generate required elements for destiny choice.
		}

		return null;
	}
}

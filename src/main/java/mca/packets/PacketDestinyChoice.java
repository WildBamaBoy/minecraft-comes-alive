package mca.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.core.forge.EventHooksFML;
import mca.core.minecraft.ModBlocks;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumDestinyChoice;
import mca.enums.EnumDialogueType;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import radixcore.math.Point3D;
import radixcore.packets.AbstractPacket;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;
import radixcore.util.SchematicHandler;

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
		final EntityPlayerMP player = (EntityPlayerMP)this.getPlayer(context);
		final PlayerData data = MCA.getPlayerData(player);
		final WorldServer world = (WorldServer)player.worldObj;

		//Make sure to spawn destiny choices on the server's tick thread, not the net IO thread.
		EventHooksFML.queueDestinySpawn(packet.choice, player);

		player.worldObj.playSoundAtEntity(player, "portal.travel", 0.5F, 2.0F);
		return null;
	}
}

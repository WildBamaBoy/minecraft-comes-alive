package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketOnPlayerMarriage extends AbstractPacket implements IMessage, IMessageHandler<PacketOnPlayerMarriage, IMessage>
{
	private int playerId;
	private String playerName;
	private int spouseId;
	
	public PacketOnPlayerMarriage()
	{
	}

	public PacketOnPlayerMarriage(int playerId, String playerName, int spouseId)
	{
		this.playerId = playerId;
		this.playerName = playerName;
		this.spouseId = spouseId;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		this.playerId = byteBuf.readInt();
		this.playerName = (String)ByteBufIO.readObject(byteBuf);
		this.spouseId = byteBuf.readInt();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(playerId);
		ByteBufIO.writeObject(byteBuf, playerName);
		byteBuf.writeInt(spouseId);
	}

	@Override
	public IMessage onMessage(PacketOnPlayerMarriage packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);

		//Workaround for problems on a server.
		String displayString = MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.marry.accept", player, null, false, "\u00a7A", null);
		displayString = displayString.replace("%SpouseName%", packet.playerName);

		player.addChatMessage(new ChatComponentText(displayString));
		player.inventory.consumeInventoryItem(MCA.getInstance().itemWeddingRing);
		
		return null;
	}
}

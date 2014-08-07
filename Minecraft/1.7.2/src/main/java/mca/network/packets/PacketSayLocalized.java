package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSayLocalized extends AbstractPacket implements IMessage, IMessageHandler<PacketSayLocalized, IMessage>
{
	private boolean hasPlayer;
	private boolean hasEntity;
	private boolean hasPrefix;
	private boolean hasSuffix;
	private String playerName;
	private Integer entityId;
	private String phraseId;
	private boolean useCharacterType;
	private String prefix;
	private String suffix;

	public PacketSayLocalized()
	{
	}

	public PacketSayLocalized(EntityPlayer player, Integer speakerEntityId, String phraseId, boolean useCharacterType, String prefix, String suffix)
	{
		if (player != null)
		{
			this.playerName = player.getCommandSenderName();
		}

		this.entityId = speakerEntityId;
		this.phraseId = phraseId;
		this.useCharacterType = useCharacterType;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		hasPlayer = byteBuf.readBoolean();
		hasEntity = byteBuf.readBoolean();
		hasPrefix = byteBuf.readBoolean();
		hasSuffix = byteBuf.readBoolean();

		playerName = hasPlayer ? (String) ByteBufIO.readObject(byteBuf) : null;
		entityId = hasEntity ? (Integer) ByteBufIO.readObject(byteBuf) : -1;
		phraseId = (String) ByteBufIO.readObject(byteBuf);
		useCharacterType = byteBuf.readBoolean();

		prefix = hasPrefix ? (String) ByteBufIO.readObject(byteBuf) : null;
		suffix = hasSuffix ? (String) ByteBufIO.readObject(byteBuf) : null;
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		hasPlayer = playerName != null;
		hasEntity = entityId != null;
		hasPrefix = prefix != null;
		hasSuffix = suffix != null;

		byteBuf.writeBoolean(hasPlayer);
		byteBuf.writeBoolean(hasEntity);
		byteBuf.writeBoolean(hasPrefix);
		byteBuf.writeBoolean(hasSuffix);

		if (hasPlayer)
		{
			ByteBufIO.writeObject(byteBuf, playerName);
		}

		if (hasEntity)
		{
			ByteBufIO.writeObject(byteBuf, entityId);
		}

		ByteBufIO.writeObject(byteBuf, phraseId);

		byteBuf.writeBoolean(useCharacterType);

		if (hasPrefix)
		{
			ByteBufIO.writeObject(byteBuf, prefix);
		}

		if (hasSuffix)
		{
			ByteBufIO.writeObject(byteBuf, suffix);
		}
	}

	@Override
	public IMessage onMessage(PacketSayLocalized packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		EntityPlayer receivedPlayer = null;
		AbstractEntity entity = null;

		if (packet.hasPlayer)
		{
			receivedPlayer = player; //player.worldObj.getPlayerEntityByName(packet.playerName);
		}

		if (packet.hasEntity)
		{
			entity = (AbstractEntity)player.worldObj.getEntityByID(packet.entityId);
		}

		if (packet.entityId != -1)
		{
			if (receivedPlayer != null)
			{
				entity.lastInteractingPlayer = receivedPlayer.getCommandSenderName();
				entity.say(MCA.getInstance().getLanguageLoader().getString(packet.phraseId, receivedPlayer, entity, packet.useCharacterType, packet.prefix, packet.suffix));
			}

			else
			{
				entity.lastInteractingPlayer = player.getCommandSenderName();
				entity.say(MCA.getInstance().getLanguageLoader().getString(packet.phraseId, player, entity, packet.useCharacterType, packet.prefix, packet.suffix));
			}
		}

		//There isn't a speaker, so just add the localized string to the player's chat log.
		else
		{
			if (receivedPlayer != null)
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(packet.phraseId, receivedPlayer, null, packet.useCharacterType, packet.prefix, packet.suffix)));
			}

			else
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(packet.phraseId, player, null, packet.useCharacterType, packet.prefix, packet.suffix)));
			}
		}

		return null;
	}
}
package mca.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

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
		this.playerName = player.getCommandSenderName();
		this.entityId = speakerEntityId;
		this.phraseId = phraseId;
		this.useCharacterType = useCharacterType;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
//		hasPlayer = byteBuf.readBoolean();
//		hasEntity = byteBuf.readBoolean();
//		hasPrefix = byteBuf.readBoolean();
//		hasSuffix = byteBuf.readBoolean();

//		String playerName = hasPlayer ? (String) ByteBufIO.readObject(byteBuf) : null;
//		entityId = hasEntity ? byteBuf.readInt() : -1;
//		phraseId = (String) ByteBufIO.readObject(byteBuf);
//		useCharacterType = byteBuf.readBoolean();
//		prefix = hasPrefix ? (String) ByteBufIO.readObject(byteBuf) : null;
//		suffix = hasSuffix ? (String) ByteBufIO.readObject(byteBuf) : null;
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
//		hasPlayer = playerName != null;
//		hasEntity = entityId != null;
//		hasPrefix = prefix != null;
//		hasSuffix = suffix != null;

//		byteBuf.writeBoolean(hasPlayer);
//		byteBuf.writeBoolean(hasEntity);
//		byteBuf.writeBoolean(hasPrefix);
//		byteBuf.writeBoolean(hasSuffix);
//
//		if (hasPlayer)
//		{
//			ByteBufIO.writeObject(byteBuf, playerName);
//		}
//
//		if (hasEntity)
//		{
//			ByteBufIO.writeObject(byteBuf, entityId);
//		}
//
//		ByteBufIO.writeObject(byteBuf, phraseId);
//		byteBuf.writeBoolean(useCharacterType);
//
//		if (hasPrefix)
//		{
//			ByteBufIO.writeObject(byteBuf, prefix);
//		}
//
//		if (hasSuffix)
//		{
//			ByteBufIO.writeObject(byteBuf, suffix);
//		}
	}

	@Override
	public IMessage onMessage(PacketSayLocalized packet, MessageContext context) 
	{
//		final EntityPlayer player = getPlayer(context);
//		EntityPlayer receivedPlayer = null;
//		AbstractEntity entity = null;
//
//		if (hasPlayer)
//		{
//			receivedPlayer = player.worldObj.getPlayerEntityByName(playerName);
//		}
//
//		if (hasEntity)
//		{
//			entity = (AbstractEntity)player.worldObj.getEntityByID(entityId);
//		}
//
//		if (entityId != -1)
//		{
//			if (receivedPlayer != null)
//			{
//				entity.lastInteractingPlayer = receivedPlayer.getCommandSenderName();
//				entity.say(MCA.getInstance().getLanguageLoader().getString(phraseId, receivedPlayer, entity, useCharacterType, prefix, suffix));
//			}
//
//			else
//			{
//				entity.lastInteractingPlayer = player.getCommandSenderName();
//				entity.say(MCA.getInstance().getLanguageLoader().getString(phraseId, player, entity, useCharacterType, prefix, suffix));
//			}
//		}
//
//		//There isn't a speaker, so just add the localized string to the player's chat log.
//		else
//		{
//			if (receivedPlayer != null)
//			{
//				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(phraseId, receivedPlayer, null, useCharacterType, prefix, suffix)));
//			}
//
//			else
//			{
//				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString(phraseId, player, null, useCharacterType, prefix, suffix)));
//			}
//		}
//		
		return null;
	}
}

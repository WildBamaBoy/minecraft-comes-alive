package mca.network.packets;

import io.netty.buffer.ByteBuf;
import mca.core.MCA;
import mca.core.WorldPropertiesList;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketBabyInfo  extends AbstractPacket implements IMessage, IMessageHandler<PacketBabyInfo, IMessage>
{
	private String targetSpouseName;
	
	private String babyName;
	private boolean babyExists;
	private boolean babyIsMale;
	private boolean babyReadyToGrow;
	
	public PacketBabyInfo()
	{
	}
	
	public PacketBabyInfo(WorldPropertiesManager manager)
	{
		this.targetSpouseName = MCA.getInstance().getWorldProperties(manager).playerSpouseName;
		
		this.babyName = MCA.getInstance().getWorldProperties(manager).babyName;
		this.babyExists = MCA.getInstance().getWorldProperties(manager).babyExists;
		this.babyIsMale = MCA.getInstance().getWorldProperties(manager).babyIsMale;
		this.babyReadyToGrow = MCA.getInstance().getWorldProperties(manager).babyReadyToGrow;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		targetSpouseName = (String) ByteBufIO.readObject(byteBuf);
		babyName = (String) ByteBufIO.readObject(byteBuf);
		babyExists = byteBuf.readBoolean();
		babyIsMale = byteBuf.readBoolean();
		babyReadyToGrow = byteBuf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		ByteBufIO.writeObject(byteBuf, targetSpouseName);
		ByteBufIO.writeObject(byteBuf, babyName);
		byteBuf.writeBoolean(babyExists);
		byteBuf.writeBoolean(babyIsMale);
		byteBuf.writeBoolean(babyReadyToGrow);
	}

	@Override
	public IMessage onMessage(PacketBabyInfo packet, MessageContext context) 
	{
		//Set the player's spouse's manager to have the same baby info.
		final WorldPropertiesManager spouseManager = MCA.getInstance().playerWorldManagerMap.get(targetSpouseName);
		final WorldPropertiesList properties = (WorldPropertiesList)spouseManager.worldPropertiesInstance;
		
		properties.babyExists = packet.babyExists;
		properties.babyIsMale = packet.babyIsMale;
		properties.babyName = packet.babyName;
		properties.babyReadyToGrow = packet.babyReadyToGrow;

		spouseManager.saveWorldProperties();
		
		return null;
	}
}

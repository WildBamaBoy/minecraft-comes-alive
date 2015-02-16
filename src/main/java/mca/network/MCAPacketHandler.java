package mca.network;

import mca.packets.PacketBabyName;
import mca.packets.PacketDestinyChoice;
import mca.packets.PacketGift;
import mca.packets.PacketInteract;
import mca.packets.PacketOpenBabyNameGUI;
import mca.packets.PacketOpenGUIOnEntity;
import mca.packets.PacketSetTutorialMessage;
import mca.packets.PacketSyncPlayerMemory;
import radixcore.network.AbstractPacketHandler;
import radixcore.packets.PacketDataContainer;
import cpw.mods.fml.relauncher.Side;

public class MCAPacketHandler extends AbstractPacketHandler
{
	public MCAPacketHandler(String modId) 
	{
		super(modId);
	}

	@Override
	public void registerPackets() 
	{
		this.registerPacket(PacketGift.class, Side.SERVER);
		this.registerPacket(PacketInteract.class, Side.SERVER);
		this.registerPacket(PacketDataContainer.class, Side.CLIENT);
		this.registerPacket(PacketOpenGUIOnEntity.class, Side.CLIENT);
		this.registerPacket(PacketSyncPlayerMemory.class, Side.CLIENT);
		this.registerPacket(PacketSetTutorialMessage.class, Side.CLIENT);
		this.registerPacket(PacketBabyName.class, Side.SERVER);
		this.registerPacket(PacketOpenBabyNameGUI.class, Side.CLIENT);
		this.registerPacket(PacketDestinyChoice.class, Side.SERVER);
	}
}

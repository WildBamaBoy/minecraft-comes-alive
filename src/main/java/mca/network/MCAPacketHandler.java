package mca.network;

import mca.packets.PacketBabyName;
import mca.packets.PacketDestinyChoice;
import mca.packets.PacketGift;
import mca.packets.PacketInteract;
import mca.packets.PacketInteractWithPlayerC;
import mca.packets.PacketInteractWithPlayerS;
import mca.packets.PacketOpenBabyNameGUI;
import mca.packets.PacketOpenGUIOnEntity;
import mca.packets.PacketOpenPrompt;
import mca.packets.PacketSetTutorialMessage;
import mca.packets.PacketSyncPlayerMemory;
import mca.packets.PacketToggleAI;
import mca.packets.PacketTombstoneUpdateGet;
import mca.packets.PacketTombstoneUpdateSet;
import net.minecraftforge.fml.relauncher.Side;
import radixcore.network.AbstractPacketHandler;
import radixcore.packets.PacketDataContainer;

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
		this.registerPacket(PacketToggleAI.class, Side.SERVER);
		this.registerPacket(PacketTombstoneUpdateSet.class, Side.SERVER);
		this.registerPacket(PacketTombstoneUpdateSet.class, Side.CLIENT);
		this.registerPacket(PacketTombstoneUpdateGet.class, Side.SERVER);
		this.registerPacket(PacketInteractWithPlayerC.class, Side.CLIENT);
		this.registerPacket(PacketInteractWithPlayerS.class, Side.SERVER);
		this.registerPacket(PacketOpenPrompt.class, Side.CLIENT);
	}
}

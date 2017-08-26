package mca.network;

import mca.packets.PacketBabyName;
import mca.packets.PacketCallVillager;
import mca.packets.PacketDestinyChoice;
import mca.packets.PacketEditVillager;
import mca.packets.PacketGift;
import mca.packets.PacketInteract;
import mca.packets.PacketInteractWithPlayerC;
import mca.packets.PacketInteractWithPlayerS;
import mca.packets.PacketMemorialUpdateGet;
import mca.packets.PacketMemorialUpdateSet;
import mca.packets.PacketOpenBabyNameGUI;
import mca.packets.PacketOpenGUIOnEntity;
import mca.packets.PacketOpenPrompt;
import mca.packets.PacketOpenVillagerPrompt;
import mca.packets.PacketPlayerDataC;
import mca.packets.PacketPlayerDataLogin;
import mca.packets.PacketPlayerDataS;
import mca.packets.PacketRelatedVillagers;
import mca.packets.PacketRequestRelatedVillagers;
import mca.packets.PacketSetSize;
import mca.packets.PacketSetTutorialMessage;
import mca.packets.PacketSpawnLightning;
import mca.packets.PacketSyncConfig;
import mca.packets.PacketSyncPlayerMemory;
import mca.packets.PacketToggleAI;
import net.minecraftforge.fml.relauncher.Side;
import radixcore.modules.net.AbstractPacketHandler;

public class PacketHandlerMCA extends AbstractPacketHandler
{
	public PacketHandlerMCA(String modId) 
	{
		super(modId);
	}

	@Override
	public void registerPackets() 
	{
		this.registerPacket(PacketGift.class, Side.SERVER);
		this.registerPacket(PacketInteract.class, Side.SERVER);
		this.registerPacket(PacketOpenGUIOnEntity.class, Side.CLIENT);
		this.registerPacket(PacketSyncPlayerMemory.class, Side.CLIENT);
		this.registerPacket(PacketSetTutorialMessage.class, Side.CLIENT);
		this.registerPacket(PacketBabyName.class, Side.SERVER);
		this.registerPacket(PacketOpenBabyNameGUI.class, Side.CLIENT);
		this.registerPacket(PacketDestinyChoice.class, Side.SERVER);
		this.registerPacket(PacketToggleAI.class, Side.SERVER);
		this.registerPacket(PacketInteractWithPlayerC.class, Side.CLIENT);
		this.registerPacket(PacketInteractWithPlayerS.class, Side.SERVER);
		this.registerPacket(PacketOpenPrompt.class, Side.CLIENT);
		this.registerPacket(PacketSyncConfig.class, Side.CLIENT);
		this.registerPacket(PacketSetSize.class, Side.CLIENT);
		this.registerPacket(PacketRequestRelatedVillagers.class, Side.SERVER);
		this.registerPacket(PacketRelatedVillagers.class, Side.CLIENT);
		this.registerPacket(PacketCallVillager.class, Side.SERVER);
		this.registerPacket(PacketSpawnLightning.class, Side.CLIENT);
		this.registerPacket(PacketMemorialUpdateGet.class, Side.SERVER);
		this.registerPacket(PacketMemorialUpdateSet.class, Side.CLIENT);
		this.registerPacket(PacketPlayerDataS.class, Side.SERVER);
		this.registerPacket(PacketPlayerDataC.class, Side.CLIENT);
		this.registerPacket(PacketPlayerDataLogin.class, Side.CLIENT);
		this.registerPacket(PacketOpenVillagerPrompt.class, Side.CLIENT);
		this.registerPacket(PacketEditVillager.class, Side.SERVER);
	}
}

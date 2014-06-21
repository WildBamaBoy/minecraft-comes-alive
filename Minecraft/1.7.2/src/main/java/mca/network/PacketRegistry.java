package mca.network;

import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketAddBaby;
import mca.network.packets.PacketBabyInfo;
import mca.network.packets.PacketClickAid;
import mca.network.packets.PacketClickMountHorse;
import mca.network.packets.PacketClickTakeGift;
import mca.network.packets.PacketClientCommand;
import mca.network.packets.PacketGetTombstoneText;
import mca.network.packets.PacketNotifyPlayer;
import mca.network.packets.PacketOnClickTrade;
import mca.network.packets.PacketOnEngagement;
import mca.network.packets.PacketOnPlayerMarriage;
import mca.network.packets.PacketOnPlayerProcreate;
import mca.network.packets.PacketOnVillagerProcreate;
import mca.network.packets.PacketOpenGui;
import mca.network.packets.PacketRemoveBabyRequest;
import mca.network.packets.PacketRemoveItem;
import mca.network.packets.PacketRemoveMarriageRequest;
import mca.network.packets.PacketReturnInventory;
import mca.network.packets.PacketSayLocalized;
import mca.network.packets.PacketSetChore;
import mca.network.packets.PacketSetFamilyTree;
import mca.network.packets.PacketSetFieldValue;
import mca.network.packets.PacketSetInventory;
import mca.network.packets.PacketSetPosition;
import mca.network.packets.PacketSetTarget;
import mca.network.packets.PacketSetTombstoneText;
import mca.network.packets.PacketSetWorldProperties;
import mca.network.packets.PacketStopJumping;
import mca.network.packets.PacketSwingArm;
import mca.network.packets.PacketSync;
import mca.network.packets.PacketSyncEditorSettings;
import mca.network.packets.PacketSyncRequest;
import mca.network.packets.PacketUpdateFurnace;

import com.radixshock.radixcore.core.IEnforcedCore;
import com.radixshock.radixcore.network.AbstractPacketHandler;

public class PacketRegistry extends AbstractPacketHandler
{
	public PacketRegistry(IEnforcedCore ownerMod) 
	{
		super(ownerMod);
	}

	@Override
	public void registerPackets()
	{
		this.registerDoubleSidedPacket(PacketAddAI.class, 0);
		this.registerDoubleSidedPacket(PacketAddBaby.class, 1);
		this.registerDoubleSidedPacket(PacketBabyInfo.class, 2);
		this.registerDoubleSidedPacket(PacketClickAid.class, 3);
		this.registerDoubleSidedPacket(PacketClickMountHorse.class, 4);
		this.registerDoubleSidedPacket(PacketClickTakeGift.class, 5);
		this.registerDoubleSidedPacket(PacketClientCommand.class, 6);
		this.registerDoubleSidedPacket(PacketGetTombstoneText.class, 7);
		this.registerDoubleSidedPacket(PacketNotifyPlayer.class, 8);
		this.registerDoubleSidedPacket(PacketOnClickTrade.class, 9);
		this.registerDoubleSidedPacket(PacketOnEngagement.class, 10);
		this.registerDoubleSidedPacket(PacketOnPlayerMarriage.class, 11);
		this.registerDoubleSidedPacket(PacketOnPlayerProcreate.class, 12);
		this.registerDoubleSidedPacket(PacketOnVillagerProcreate.class, 13);
		this.registerDoubleSidedPacket(PacketOpenGui.class, 14);
		this.registerDoubleSidedPacket(PacketRemoveBabyRequest.class, 15);
		this.registerDoubleSidedPacket(PacketRemoveItem.class, 16);
		this.registerDoubleSidedPacket(PacketRemoveMarriageRequest.class, 17);
		this.registerDoubleSidedPacket(PacketReturnInventory.class, 18);
		this.registerDoubleSidedPacket(PacketSayLocalized.class, 19);
		this.registerDoubleSidedPacket(PacketSetChore.class, 20);
		this.registerDoubleSidedPacket(PacketSetFamilyTree.class, 21);
		this.registerDoubleSidedPacket(PacketSetFieldValue.class, 22);
		this.registerDoubleSidedPacket(PacketSetInventory.class, 23);
		this.registerDoubleSidedPacket(PacketSetPosition.class, 24);
		this.registerDoubleSidedPacket(PacketSetTarget.class, 25);
		this.registerDoubleSidedPacket(PacketSetTombstoneText.class, 26);
		this.registerDoubleSidedPacket(PacketSetWorldProperties.class, 27);
		this.registerDoubleSidedPacket(PacketStopJumping.class, 28);
		this.registerDoubleSidedPacket(PacketSwingArm.class, 29);
		this.registerDoubleSidedPacket(PacketSync.class, 30);
		this.registerDoubleSidedPacket(PacketSyncEditorSettings.class, 31);
		this.registerDoubleSidedPacket(PacketSyncRequest.class, 32);
		this.registerDoubleSidedPacket(PacketUpdateFurnace.class, 33);
	}
}

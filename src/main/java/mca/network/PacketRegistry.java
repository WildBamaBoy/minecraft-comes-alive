/*******************************************************************************
 * PacketRegistry.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network;

import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketAddBaby;
import mca.network.packets.PacketBabyInfo;
import mca.network.packets.PacketClickAid;
import mca.network.packets.PacketClickMountHorse;
import mca.network.packets.PacketClickTakeGift;
import mca.network.packets.PacketGetTombstoneText;
import mca.network.packets.PacketNotifyLocalized;
import mca.network.packets.PacketNotifyPlayer;
import mca.network.packets.PacketOnClickTrade;
import mca.network.packets.PacketOnEngagement;
import mca.network.packets.PacketOnPlayerMarriage;
import mca.network.packets.PacketOnVillagerProcreate;
import mca.network.packets.PacketOpenGui;
import mca.network.packets.PacketPlayerInteraction;
import mca.network.packets.PacketProcreate;
import mca.network.packets.PacketRemoveItem;
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
		registerDoubleSidedPacket(PacketAddAI.class, 0);
		registerDoubleSidedPacket(PacketAddBaby.class, 1);
		registerDoubleSidedPacket(PacketBabyInfo.class, 2);
		registerDoubleSidedPacket(PacketClickAid.class, 3);
		registerDoubleSidedPacket(PacketClickMountHorse.class, 4);
		registerDoubleSidedPacket(PacketClickTakeGift.class, 5);
		registerDoubleSidedPacket(PacketGetTombstoneText.class, 7);
		registerDoubleSidedPacket(PacketNotifyPlayer.class, 8);
		registerDoubleSidedPacket(PacketOnClickTrade.class, 9);
		registerDoubleSidedPacket(PacketOnEngagement.class, 10);
		registerDoubleSidedPacket(PacketOnPlayerMarriage.class, 11);
		registerDoubleSidedPacket(PacketOnVillagerProcreate.class, 13);
		registerDoubleSidedPacket(PacketOpenGui.class, 14);
		registerDoubleSidedPacket(PacketRemoveItem.class, 16);
		registerDoubleSidedPacket(PacketReturnInventory.class, 18);
		registerDoubleSidedPacket(PacketSayLocalized.class, 19);
		registerDoubleSidedPacket(PacketSetChore.class, 20);
		registerDoubleSidedPacket(PacketSetFamilyTree.class, 21);
		registerDoubleSidedPacket(PacketSetFieldValue.class, 22);
		registerDoubleSidedPacket(PacketSetInventory.class, 23);
		registerDoubleSidedPacket(PacketSetPosition.class, 24);
		registerDoubleSidedPacket(PacketSetTarget.class, 25);
		registerDoubleSidedPacket(PacketSetTombstoneText.class, 26);
		registerDoubleSidedPacket(PacketSetWorldProperties.class, 27);
		registerDoubleSidedPacket(PacketStopJumping.class, 28);
		registerDoubleSidedPacket(PacketSwingArm.class, 29);
		registerDoubleSidedPacket(PacketSync.class, 30);
		registerDoubleSidedPacket(PacketSyncEditorSettings.class, 31);
		registerDoubleSidedPacket(PacketSyncRequest.class, 32);
		registerDoubleSidedPacket(PacketUpdateFurnace.class, 33);
		registerDoubleSidedPacket(PacketPlayerInteraction.class, 34);
		registerDoubleSidedPacket(PacketNotifyLocalized.class, 35);
		registerDoubleSidedPacket(PacketProcreate.class, 36);
	}
}

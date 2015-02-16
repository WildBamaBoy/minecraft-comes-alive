package radixcore.network;

import radixcore.packets.PacketDataSync;
import radixcore.packets.PacketDataSyncReq;
import radixcore.packets.PacketWatchedUpdateC;
import radixcore.packets.PacketWatchedUpdateS;
import cpw.mods.fml.relauncher.Side;

public class RadixPacketHandler extends AbstractPacketHandler
{
	public RadixPacketHandler(String modId) 
	{
		super(modId);
		
	}

	@Override
	public void registerPackets() 
	{
		this.registerPacket(PacketWatchedUpdateC.class, Side.CLIENT);
		this.registerPacket(PacketWatchedUpdateS.class, Side.SERVER);
		this.registerPacket(PacketDataSyncReq.class, Side.SERVER);
		this.registerPacket(PacketDataSync.class, Side.CLIENT);
	}
}

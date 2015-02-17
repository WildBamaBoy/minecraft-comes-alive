package mca.core.forge;

import mca.core.MCA;
import mca.network.MCAPacketHandler;

public class ServerProxy 
{
	public void registerRenderers()
	{
		//Server-side, no rendering.
	}

	public MCAPacketHandler registerPackets() 
	{
    	MCAPacketHandler handler = new MCAPacketHandler(MCA.ID);
    	return handler;
	}
}

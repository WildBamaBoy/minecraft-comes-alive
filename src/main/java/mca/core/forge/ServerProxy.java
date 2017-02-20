package mca.core.forge;

import mca.core.MCA;
import mca.network.PacketHandlerMCA;

public class ServerProxy 
{
	public void registerEntityRenderers()
	{
		//Server-side, no rendering.
	}

	public void registerEventHandlers()
	{
		
	}
	
	public void registerModelMeshers()
	{
		
	}
	
	public PacketHandlerMCA registerPackets() 
	{
    	PacketHandlerMCA handler = new PacketHandlerMCA(MCA.ID);
    	return handler;
	}
}

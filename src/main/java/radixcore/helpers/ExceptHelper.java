package radixcore.helpers;

import radixcore.RadixCore;

public final class ExceptHelper 
{
	public static void logErrorCatch(Throwable t, String description)
	{
		RadixCore.getLogger().catching(t);
		RadixCore.getLogger().error("Unexpected exception/(" + description +"). " + t.getMessage());
	}
	
	public static void logFatalCatch(Throwable t, String description)
	{
		RadixCore.getLogger().catching(t);
		RadixCore.getLogger().fatal("Unexpected exception/(" + description +"). " + t.getMessage());
		throw new RuntimeException("Caught fatal exception and stopped the game. Please review your logs for crash details.");
	}
	
	private ExceptHelper()
	{
		
	}
}

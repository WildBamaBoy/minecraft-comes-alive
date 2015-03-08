package radixcore.update;

import java.net.URL;
import java.util.Scanner;

import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;

public class ScannerUpdateProtocol implements IUpdateProtocol
{
	private Scanner scanner;
	
	@Override
	public UpdateData getUpdateData(ModMetadataEx exData) 
	{
		UpdateData returnData = null;
		
		try
		{
			final URL url = new URL(exData.updateUrl);
			scanner = new Scanner(url.openStream());

			returnData = new UpdateData();
			returnData.modVersion = scanner.nextLine();
			returnData.minecraftVersion = scanner.nextLine();
			
			scanner.close();
		}

		catch (final Exception e)
		{
			RadixCore.getLogger().error("Unexpected exception during update checking for " + exData.name + ". Error was: " + e.getMessage());
		}

		return returnData;
	}

	@Override
	public void cleanUp() 
	{
		if (scanner != null)
		{
			scanner.close();
		}
	}
}

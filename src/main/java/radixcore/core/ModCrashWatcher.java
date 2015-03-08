package radixcore.core;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

public abstract class ModCrashWatcher 
{
	private final long startupTimestamp;
	
	public ModCrashWatcher()
	{
		startupTimestamp = new Date().getTime();
	}
	
	public void checkForCrashReports()
	{
		File crashReportsFolder = new File(RadixCore.getRunningDirectory() + "/crash-reports/");

		try
		{
			File[] files = crashReportsFolder.listFiles(new FileFilter() 
			{			
				public boolean accept(File file) 
				{
					return file.isFile();
				}
			});
			
			long lastModifiedTime = Long.MIN_VALUE;
			File lastModifiedFile = null;
			
			for (File file : files) 
			{
				if (file.lastModified() > lastModifiedTime) 
				{
					lastModifiedFile = file;
					lastModifiedTime = file.lastModified();
				}
			}
			
			if (lastModifiedTime > startupTimestamp)
			{
				onCrash(lastModifiedFile);
			}
		}

		catch (Throwable e)
		{
		}
	}
	
	protected abstract void onCrash(File crashFile);
}

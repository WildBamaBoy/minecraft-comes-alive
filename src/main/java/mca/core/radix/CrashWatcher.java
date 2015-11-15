package mca.core.radix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Scanner;

import cpw.mods.fml.common.FMLCommonHandler;
import mca.core.MCA;
import radixcore.core.ModCrashWatcher;

public class CrashWatcher extends ModCrashWatcher
{
	@Override
	protected void onCrash(File crashFile) 
	{
		try
		{
			boolean isServer = FMLCommonHandler.instance().getEffectiveSide().isServer();

			//Get the report into a form we can easily work with.
			Scanner scanner = new Scanner(crashFile);
			String report = scanner.useDelimiter("\\Z").next();
			scanner.close();
			
			String[] lineByLine = report.split("\\r?\\n");
			String[] stackTrace = new String[lineByLine.length];
			int stackIndex = 0;
			
			for (int i = 0; i < lineByLine.length; i++)
			{
				String line = lineByLine[i];
				
				if (line.startsWith("\tat"))
				{
					stackTrace[stackIndex] = lineByLine[i];
					stackIndex++;
				}
			}
			
			//Determine whether MCA is the cause of the problem, or whether a mod simply ran through it. The first part of the stack trace should include "at mca."
			if (stackTrace[0].contains("at mca."))
			{
				if (MCA.getConfig().allowCrashReporting)
				{
					final Socket connectSocket = new Socket("asp.radix-shock.com", 3577);
					final DataOutputStream dataOut = new DataOutputStream(connectSocket.getOutputStream());
					new DataInputStream(connectSocket.getInputStream());
					dataOut.writeByte(2);
					dataOut.writeUTF("@Validate@");
					dataOut.writeUTF("MCA");
					dataOut.writeUTF(MCA.VERSION);
					dataOut.writeBoolean(isServer);
					dataOut.writeUTF(report);
					connectSocket.close();

					MCA.getLog().fatal("Sent crash report to mod authors for review. Sorry about that!");
				}

				else
				{
					Thread.sleep(1000); //Give the crash report time to be displayed to the console so this message appears after the fact.
					MCA.getLog().fatal("Detected a crash involving MCA, but crash reporting has been disabled! :(");
					MCA.getLog().fatal("Please consider enabling crash reporting. It will help us find and stop crashes such as this!");
				}
			}
		}

		catch (Exception e)
		{
			MCA.getLog().fatal("MCA detected a crash and attempted to report it, but failed to do so! " + e.getMessage());
		}
	}
}

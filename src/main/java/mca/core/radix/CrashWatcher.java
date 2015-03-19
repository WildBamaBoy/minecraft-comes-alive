package mca.core.radix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Scanner;

import cpw.mods.fml.common.FMLCommonHandler;
import mca.core.Constants;
import mca.core.MCA;
import radixcore.core.ModCrashWatcher;

public class CrashWatcher extends ModCrashWatcher
{
	@Override
	protected void onCrash(File crashFile) 
	{
		try
		{
			String report = new Scanner(crashFile).useDelimiter("\\Z").next();
			boolean isServer = FMLCommonHandler.instance().getEffectiveSide().isServer();
			
			if (report.contains("at mca."))
			{
				final Socket connectSocket = new Socket("vps.radix-shock.com", 3577);
				final DataOutputStream dataOut = new DataOutputStream(connectSocket.getOutputStream());
				new DataInputStream(connectSocket.getInputStream());
				dataOut.writeByte(2);
				dataOut.writeUTF("MCA");
				dataOut.writeUTF(MCA.VERSION);
				dataOut.writeBoolean(isServer);
				dataOut.writeUTF(report);
				connectSocket.close();
			}
		}

		catch (Exception e)
		{
			MCA.getLog().fatal("MCA detected a crash and attempted to report it, but failed to do so! " + e.getMessage());
		}
	}
}

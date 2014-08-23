package mca.frontend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public final class RDXServerBridge 
{
	public static String sendVersionQuery()
	{
		String version = null;
		
		try
		{
			Socket connectSocket = new Socket("107.170.27.20", 3577);
	
			DataOutputStream dataOut = new DataOutputStream(connectSocket.getOutputStream());
			DataInputStream dataIn = new DataInputStream(connectSocket.getInputStream());
	
			dataOut.writeByte(1);
			dataOut.writeUTF("MCA");
			
			version = dataIn.readUTF();
			
			connectSocket.close();
		}
	
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		
		return version;
	}

	public static void sendCrashReport(String completeReport)
	{
		try
		{
			Socket connectSocket = new Socket("107.170.27.20", 3577);

			DataOutputStream dataOut = new DataOutputStream(connectSocket.getOutputStream());
			DataInputStream dataIn = new DataInputStream(connectSocket.getInputStream());

			dataOut.writeByte(2);
			dataOut.writeUTF("MCA");
			dataOut.writeUTF(completeReport);

			connectSocket.close();
		}

		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}

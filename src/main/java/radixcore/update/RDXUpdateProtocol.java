package radixcore.update;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;

public class RDXUpdateProtocol implements IUpdateProtocol
{
	private Socket connectSocket;
	
	@Override
	public UpdateData getUpdateData(ModMetadataEx modData) 
	{
		UpdateData returnData = null;
		
		try
		{
			connectSocket = new Socket("vps.radix-shock.com", 3577);
			final DataOutputStream dataOut = new DataOutputStream(connectSocket.getOutputStream());
			final DataInputStream dataIn = new DataInputStream(connectSocket.getInputStream());

			dataOut.writeByte(1);
			dataOut.writeUTF("@Validate@");
			dataOut.writeUTF(modData.modId);
			dataOut.writeUTF(modData.version);

			String dataRecv = dataIn.readUTF();
			
			returnData = new UpdateData();
			returnData.modVersion = dataRecv.substring(0, dataRecv.indexOf("|"));
			returnData.minecraftVersion = dataRecv.substring(dataRecv.indexOf("|") + 1);
			
			connectSocket.close();
		}

		catch (final Throwable e)
		{
			e.printStackTrace();
		}

		return returnData;
	}

	@Override
	public void cleanUp() 
	{
		if (connectSocket != null && !connectSocket.isClosed())
		{
			try
			{
				connectSocket.close();
			}
			
			catch (IOException e)
			{
				RadixCore.getLogger().error("Unexpected exception while cleaning up update checker. Error was: " + e.getMessage());
			}
		}
	}
}

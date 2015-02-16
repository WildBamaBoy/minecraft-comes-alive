package radixcore.data;

import java.io.Serializable;

import net.minecraft.client.Minecraft;

public class DataContainer implements Serializable
{
	private AbstractPlayerData data;

	public DataContainer(AbstractPlayerData data)
	{
		this.data = data;
		this.data.owner = Minecraft.getMinecraft().thePlayer;
		this.data.dataWatcher.setObjectOwner(data);
	}

	public <T extends AbstractPlayerData> T getPlayerData(Class<T> type)
	{
		return (T) data;
	}
}

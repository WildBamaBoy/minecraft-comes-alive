package radixcore.update;

import radixcore.ModMetadataEx;

public interface IUpdateProtocol 
{
	UpdateData getUpdateData(ModMetadataEx modData);
	
	void cleanUp();
}

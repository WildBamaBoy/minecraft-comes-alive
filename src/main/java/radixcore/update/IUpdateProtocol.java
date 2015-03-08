package radixcore.update;

import radixcore.core.ModMetadataEx;

public interface IUpdateProtocol 
{
	UpdateData getUpdateData(ModMetadataEx modData);
	
	void cleanUp();
}

package radixcore.data;

/**
 * Implement on an entity that needs to have a permanent entity ID. Replaces a UUID cross-versions.
 */
public interface IPermanent 
{
	int getPermanentId();
	
	void setPermanentId(int value);
}

package radixcore.data;

import java.io.Serializable;


public abstract class AbstractWatched implements Serializable
{
	protected DataWatcherEx dataWatcher;
	protected int dataWatcherId;
	
	protected AbstractWatched(Object value, DataWatcherEx dataWatcher, int dataWatcherId)
	{
		if (this.dataWatcher != null)
		{
			throw new UnsupportedOperationException("Watched object is already initialized!");
		}

		else
		{
			if (this instanceof WatchedBoolean)
			{
				value = ((Boolean)value) == false ? 0 : 1;
			}
			
			this.dataWatcher = dataWatcher;
			this.dataWatcherId = dataWatcherId;
			dataWatcher.addObject(dataWatcherId, value);
		}
	}
	
	public Byte getByte()
	{
		return dataWatcher.getWatchableObjectByte(dataWatcherId);
	}
	
	public Float getFloat()
	{
		return dataWatcher.getWatchableObjectFloat(dataWatcherId);
	}
	
	public String getString()
	{
		return dataWatcher.getWatchableObjectString(dataWatcherId);
	}
	
	public int getInt()
	{
		return dataWatcher.getWatchableObjectInt(dataWatcherId);
	}
	
	public Short getShort()
	{
		return dataWatcher.getWatchableObjectShort(dataWatcherId);
	}
	
	public Boolean getBoolean()
	{
		return dataWatcher.getWatchableObjectInt(dataWatcherId) == 0 ? false : true;
	}
	
	public void setValue(Object value)
	{
		if (value instanceof Boolean)
		{
			value = ((Boolean)value) == false ? 0 : 1;
		}
		
		dataWatcher.updateObject(dataWatcherId, value, true);
	}
}

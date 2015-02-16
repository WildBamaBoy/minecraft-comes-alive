package radixcore.data;

import java.io.Serializable;

import radixcore.enums.EnumWatchedObjectType;

public final class WatchedObjectEx implements Serializable
{
    private final int objectType;
    private final int dataValueId;
    private Object watchedObject;
    private boolean watched;

    public WatchedObjectEx(int objectType, int dataValueId, Object watchedObject)
    {
        this.dataValueId = dataValueId;
        this.watchedObject = watchedObject;
        this.objectType = objectType;
        this.watched = true;
    }

    public int getDataValueId()
    {
        return this.dataValueId;
    }

    public void setObject(Object value)
    {
        this.watchedObject = value;
    }

    public Object getObject()
    {
        return this.watchedObject;
    }

    public EnumWatchedObjectType getObjectType()
    {
        return EnumWatchedObjectType.getById(this.objectType);
    }

    public boolean isWatched()
    {
        return this.watched;
    }

    public void setWatched(boolean value)
    {
        this.watched = value;
    }
}
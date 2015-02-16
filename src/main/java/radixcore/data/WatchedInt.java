package radixcore.data;

public class WatchedInt extends AbstractWatched
{
	public WatchedInt(int value, int dataWatcherId, DataWatcherEx dataWatcher)
	{
		super(value, dataWatcher, dataWatcherId);
	}
}

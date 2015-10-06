package mca.test;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;

/**
 * A fake world object used during unit testing.
 */
public class DummyWorld extends World
{
	private DummySaveHandler saveHandler;
	private List<Object> loadedEntityList;
	
	public DummyWorld()
	{
		super(null, null, WorldProvider.getProviderForDimension(0), new WorldSettings(1, WorldSettings.GameType.NOT_SET, true, true, WorldType.FLAT), null);
		this.saveHandler = new DummySaveHandler();
		this.loadedEntityList = new ArrayList<Object>();
	}

	
	@Override
	public boolean spawnEntityInWorld(Entity entity) 
	{
		entity.setWorld(this);
		return loadedEntityList.add(entity);
	}

	@Override
	protected IChunkProvider createChunkProvider() 
	{
		return null;
	}

	@Override
	public ISaveHandler getSaveHandler() 
	{
		return saveHandler;
	}

	@Override
	protected int func_152379_p() 
	{
		return 0; //Required by implementation.
	}

	@Override
	public Entity getEntityByID(int id) 
	{
		return null; //Required by implementation.
	}

	@Override
	public List getLoadedEntityList() 
	{
		return loadedEntityList;
	}
}

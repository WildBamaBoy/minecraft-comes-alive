package mca.test;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
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
		super(null, null, DimensionType.getById(0).createDimension(), null, false);
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
	public Entity getEntityByID(int id) 
	{
		return null; //Required by implementation.
	}

	@Override
	public List getLoadedEntityList() 
	{
		return loadedEntityList;
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) 
	{
		return false;
	}
}

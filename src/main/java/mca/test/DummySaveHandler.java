package mca.test;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * A fake save handler used during unit testing.
 */
public class DummySaveHandler implements ISaveHandler
{
	public DummySaveHandler()
	{	
	}
	
	@Override
	public WorldInfo loadWorldInfo() 
	{
		return null;
	}

	@Override
	public void checkSessionLock() throws MinecraftException 
	{
		
	}

	@Override
	public IChunkLoader getChunkLoader(WorldProvider p_75763_1_) 
	{
		return null;
	}

	@Override
	public void saveWorldInfoWithPlayer(WorldInfo p_75755_1_, NBTTagCompound p_75755_2_) 
	{
		
	}

	@Override
	public void saveWorldInfo(WorldInfo p_75761_1_) 
	{
		
	}

	@Override
	public void flush() 
	{
		
	}

	@Override
	public File getWorldDirectory() 
	{
		return new File("./eclipse/junit/TestWorld/");
	}

	@Override
	public File getMapFileFromName(String p_75758_1_) 
	{
		return null;
	}

	@Override
	public String getWorldDirectoryName() 
	{
		return "TestWorld";
	}

	@Override
	public IPlayerFileData getPlayerNBTManager() 
	{
		return null;
	}

}

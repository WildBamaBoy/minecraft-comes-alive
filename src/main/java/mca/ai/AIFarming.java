package mca.ai;

import mca.api.ChoreRegistry;
import mca.api.CropEntry;
import mca.api.exception.MappingNotFoundException;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;

public class AIFarming extends AbstractToggleAI
{
	private WatchedBoolean isAIActive;
	private Point3D farmCenterPoint;
	private int apiId;
	private int radius;
	private boolean doCreate;
	
	public AIFarming(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_FARMING_ACTIVE, owner.getDataWatcherEx());
		farmCenterPoint = new Point3D(0, 0, 0);
	}

	@Override
	public void setIsActive(boolean value) 
	{
		isAIActive.setValue(value);
	}

	@Override
	public boolean getIsActive() 
	{
		return isAIActive.getBoolean();
	}

	@Override
	public void onUpdateCommon() 
	{
	}

	@Override
	public void onUpdateClient() 
	{	
	}

	@Override
	public void onUpdateServer() 
	{
		try
		{
			CropEntry crop = ChoreRegistry.getCropEntryById(apiId);
			
		}
		
		catch (MappingNotFoundException e)
		{
			reset();
		}
	}

	@Override
	public void reset() 
	{
		isAIActive.setValue(false);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isFarmingActive", isAIActive.getBoolean());
		farmCenterPoint.writeToNBT("farmCenterPoint", nbt);
		nbt.setInteger("apiId", apiId);
		nbt.setInteger("radius", radius);
		nbt.setBoolean("doCreate", doCreate);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isFarmingActive"));
		farmCenterPoint = Point3D.readFromNBT("farmCenterPoint", nbt);
		apiId = nbt.getInteger("apiId");
		radius = nbt.getInteger("radius");
		doCreate = nbt.getBoolean("doCreate");
	}

	public void startFarming(EntityPlayer player, int apiId, int radius, boolean doCreate)
	{
		this.assigningPlayer = player.getUniqueID().toString();
		this.apiId = apiId;
		this.radius = radius;
		this.isAIActive.setValue(true);
		this.doCreate = doCreate;
	}
}

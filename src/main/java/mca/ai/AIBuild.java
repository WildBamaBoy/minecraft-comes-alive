package mca.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mca.api.CropEntry;
import mca.api.enums.EnumCropCategory;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.data.BlockObj;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.SchematicHandler;

public class AIBuild extends AbstractToggleAI
{
	private Map<Point3D, BlockObj> schematicMap;
	private CropEntry cropEntry;

	private List<Point3D> blockPoints;
	private List<Point3D> torchPoints;
	private Point3D origin;
	private WatchedBoolean isAIActive;

	private String schematicName = "none";
	private int interval = 20;
	private int index = -1;
	private Block groundBlock;

	public AIBuild(EntityHuman owner) 
	{
		super(owner);
		origin = Point3D.ZERO;
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_BUILDING_ACTIVE, owner.getDataWatcherEx());
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
		if (index != -1)
		{
			interval--;

			if (interval <= 0)
			{
				interval = 20;
				owner.swingItem();

				for (int i = 0; i < 10; i++)
				{
					//Building the main schematic is complete once index is too high. Now place the torches and finish.
					if (index >= blockPoints.size())
					{
						for (Point3D point : torchPoints)
						{
							final BlockObj blockObj = schematicMap.get(point);
							BlockHelper.setBlock(owner.worldObj, origin.iPosX + point.iPosX, origin.iPosY + point.iPosY, origin.iPosZ + point.iPosZ, blockObj.getBlock());
						}

						owner.setMovementState(EnumMovementState.MOVE);
						reset();
						break;
					}

					else
					{
						final Point3D point = blockPoints.get(index);
						final BlockObj blockObj = schematicMap.get(point);

						index++;

						if (blockObj.getBlock() == Blocks.grass && groundBlock != null)
						{
							BlockHelper.setBlock(owner.worldObj, origin.iPosX + point.iPosX, origin.iPosY + point.iPosY, origin.iPosZ + point.iPosZ, groundBlock);							
						}

						else
						{
							BlockHelper.setBlock(owner.worldObj, origin.iPosX + point.iPosX, origin.iPosY + point.iPosY, origin.iPosZ + point.iPosZ, blockObj.getBlock());
						}
					}
				}
			}
		}
	}

	@Override
	public void reset() 
	{
		setIsActive(false);
		schematicMap = null;
		origin = Point3D.ZERO;
		blockPoints.clear();
		torchPoints.clear();
		groundBlock = null;
		cropEntry = null;

		schematicName = "none";
		index = -1; //-1 index indicates building is not taking place.
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setString("schematicName", schematicName);
		nbt.setInteger("interval", interval);
		nbt.setInteger("index", index);
		nbt.setInteger("originX", origin.iPosX);
		nbt.setInteger("originY", origin.iPosY);
		nbt.setInteger("originZ", origin.iPosZ);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		schematicName = nbt.getString("schematicName");
		interval = nbt.getInteger("interval");
		index = nbt.getInteger("index");

		int originX = nbt.getInteger("originX");
		int originY = nbt.getInteger("originY");
		int originZ = nbt.getInteger("originZ");

		if (index != -1)
		{
			primeSchematic(schematicName);
			origin = new Point3D(originX, originY, originZ);
		}
	}

	/**
	 * Begins building the specified schematic if possible for this area.
	 * 
	 * @param 	schematicLocation	The location of the schematic to place in the world.
	 * @param	doTopDown			Whether or not the schematic should be built from the top down.
	 * 
	 * @return	True if the building can begin. False if otherwise.
	 */
	public boolean startBuilding(String schematicLocation, boolean doTopDown)
	{
		if (RadixLogic.getNearbyBlocks(owner, Blocks.planks, 10).size() != 0)
		{			
			return false;
		}

		else
		{
			this.origin = new Point3D(owner.posX, owner.posY + 1, owner.posZ);
			this.owner.setMovementState(EnumMovementState.STAY);

			primeSchematic(schematicLocation);

			setIsActive(true);
			return true;
		}
	}

	public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock)
	{
		this.groundBlock = groundBlock;
		return startBuilding(schematicLocation, doTopDown);
	}

	public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlock, CropEntry cropEntry)
	{
		this.cropEntry = cropEntry;
		return startBuilding(schematicLocation, doTopDown, groundBlock);
	}

	private void primeSchematic(String schematicLocation)
	{
		schematicName = schematicLocation;
		blockPoints = new ArrayList<Point3D>();
		torchPoints = new ArrayList<Point3D>();
		schematicMap = SchematicHandler.readSchematic(schematicLocation);
		index = 0;

		int compareY = -25;

		for (final Map.Entry<Point3D, BlockObj> entry: schematicMap.entrySet())
		{
			final Point3D point = entry.getKey();
			Block blockAtPoint = BlockHelper.getBlock(owner.worldObj, origin.iPosX + point.iPosX, origin.iPosY + point.iPosY, origin.iPosZ + point.iPosZ);
			
			if (blockAtPoint == Blocks.tallgrass || blockAtPoint == Blocks.red_flower || blockAtPoint == Blocks.double_plant || blockAtPoint == Blocks.yellow_flower)
			{
				BlockHelper.setBlock(owner.worldObj, origin.iPosX + point.iPosX, origin.iPosY + point.iPosY, origin.iPosZ + point.iPosZ, Blocks.air);
			}
			
			compareY = -25;

			while (compareY < 25)
			{
				if (schematicMap.get(point).getBlock() == Blocks.torch)
				{
					torchPoints.add(point);
				}

				else if (point.iPosY == compareY)
				{
					blockPoints.add(point);
				}

				compareY++;
			}
		}

		//Modify the schematic as needed if a crop entry is provided.
		if (cropEntry != null)
		{
			final BlockObj searchRefBlock = new BlockObj(Blocks.wool, cropEntry.getCategory().getReferenceMeta());
			final BlockObj waterRefBlock = new BlockObj(Blocks.wool, 11);
			final BlockObj farmland = new BlockObj(Blocks.farmland, 0);
			final BlockObj cropBlock = new BlockObj(cropEntry.getCropBlock(), 0);
			final BlockObj waterBlock = new BlockObj(Blocks.water, 0);

			final Map<Point3D, BlockObj> changes = new HashMap<Point3D, BlockObj>();

			for (final Map.Entry<Point3D, BlockObj> entry : schematicMap.entrySet())
			{
				if (entry.getValue().equals(searchRefBlock))
				{
					final Point3D key = entry.getKey();
					final Point3D belowKey = SchematicHandler.getPoint3DWithValue(schematicMap, new Point3D(key.iPosX, key.iPosY - 1, key.iPosZ));

					if (belowKey != null && cropEntry.getCategory() != EnumCropCategory.SUGARCANE) 
					{
						changes.put(belowKey, farmland);
					}
					
					changes.put(entry.getKey(), cropBlock);
				}

				else if (entry.getValue().equals(waterRefBlock))
				{
					changes.put(entry.getKey(), waterBlock);
				}
			}

			for (final Map.Entry<Point3D, BlockObj> entry : changes.entrySet())
			{
				schematicMap.put(entry.getKey(), entry.getValue());
			}
		}
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
	protected String getName() 
	{
		return "Building";
	}
}

package mca.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import mca.api.CropEntry;
import mca.api.enums.EnumCropCategory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.schematics.BlockObj;
import radixcore.modules.schematics.RadixSchematics;

public class ActionBuild extends AbstractToggleAction
{	
	private Map<Point3D, BlockObj> schematicMap;
	private CropEntry cropEntry;

	private List<Point3D> blockPoints;
	private List<Point3D> torchPoints;
	private Point3D origin;

	private String schematicName = "none";
	private int interval = 20;
	private int index = -1;
	private Block groundBlock;

	public ActionBuild(EntityVillagerMCA actor) 
	{
		super(actor);
		origin = Point3D.ZERO;
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
				actor.swingItem();

				for (int i = 0; i < 10; i++)
				{
					//Building the main schematic is complete once index is too high. Now place the torches and finish.
					if (index >= blockPoints.size())
					{
						for (Point3D point : torchPoints)
						{
							final BlockObj blockObj = schematicMap.get(point);
							RadixBlocks.setBlock(actor.world, origin.iX() + point.iX(), origin.iY() + point.iY(), origin.iZ() + point.iZ(), blockObj.getBlock());
						}

						actor.attributes.setMovementState(EnumMovementState.MOVE);
						reset();
						break;
					}

					else
					{
						final Point3D point = blockPoints.get(index);
						final BlockObj blockObj = schematicMap.get(point);
						final Point3D target = new Point3D(origin.iX() + point.iX(), origin.iY() + point.iY(), origin.iZ() + point.iZ());
						index++;

						if (blockObj.getBlock() == Blocks.GRASS && groundBlock != null)
						{
							RadixBlocks.setBlock(actor.world, target, groundBlock);							
						}

						else
						{
							if (blockObj.getBlock() == Blocks.OAK_FENCE_GATE && this.schematicName.contains("mine"))
							{
								RadixBlocks.setBlock(actor.world, target.toBlockPos(), Block.getStateById(blockObj.getMeta()));
							}
							
							else
							{
								RadixBlocks.setBlock(actor.world, target, blockObj.getBlock());
							}
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
		nbt.setInteger("originX", origin.iX());
		nbt.setInteger("originY", origin.iY());
		nbt.setInteger("originZ", origin.iZ());
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
		if (RadixLogic.getNearbyBlocks(actor, Blocks.PLANKS, 10).size() != 0)
		{			
			return false;
		}

		else
		{
			this.origin = new Point3D(actor.posX, actor.posY + 1, actor.posZ);
			this.actor.attributes.setMovementState(EnumMovementState.STAY);

			primeSchematic(schematicLocation);

			setIsActive(true);
			return true;
		}
	}

	public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlockIn)
	{
		this.groundBlock = groundBlockIn;
		return startBuilding(schematicLocation, doTopDown);
	}

	public boolean startBuilding(String schematicLocation, boolean doTopDown, Block groundBlockIn, CropEntry cropEntryIn)
	{
		this.groundBlock = groundBlockIn;
		this.cropEntry = cropEntryIn;
		return startBuilding(schematicLocation, doTopDown, groundBlock);
	}

	private void primeSchematic(String schematicLocation)
	{
		SortedMap<Point3D, BlockObj> sortedSchematicMap = RadixSchematics.readSchematic(schematicLocation);
		TreeMap<Point3D, BlockObj> treeSchematicMap = new TreeMap<Point3D, BlockObj>();
		blockPoints = new ArrayList<Point3D>();
		torchPoints = new ArrayList<Point3D>();
		schematicName = schematicLocation;
		index = 0;
		
		treeSchematicMap.putAll(sortedSchematicMap);
		schematicMap = treeSchematicMap;
		
		if (schematicName.contains("mine")) //Reverse mine schematics for top down building.
		{
			schematicMap = treeSchematicMap.descendingMap();
		}

		int compareY = -25;

		for (final Map.Entry<Point3D, BlockObj> entry: schematicMap.entrySet())
		{
			final Point3D point = entry.getKey();
			Block blockAtPoint = RadixBlocks.getBlock(actor.world, origin.iX() + point.iX(), origin.iY() + point.iY(), origin.iZ() + point.iZ());
			
			if (blockAtPoint == Blocks.TALLGRASS || blockAtPoint == Blocks.RED_FLOWER || blockAtPoint == Blocks.DOUBLE_PLANT || blockAtPoint == Blocks.YELLOW_FLOWER)
			{
				RadixBlocks.setBlock(actor.world, origin.iX() + point.iX(), origin.iY() + point.iY(), origin.iZ() + point.iZ(), Blocks.AIR);
			}
			
			compareY = -25;

			while (compareY < 25)
			{
				if (schematicMap.get(point).getBlock() == Blocks.TORCH)
				{
					torchPoints.add(point);
				}

				else if (point.iY() == compareY)
				{
					blockPoints.add(point);
				}

				compareY++;
			}
		}

		//Modify the schematic as needed if a crop entry is provided.
		if (cropEntry != null)
		{
			final BlockObj searchRefBlock = new BlockObj(Blocks.WOOL, cropEntry.getCategory().getReferenceMeta());
			final BlockObj waterRefBlock = new BlockObj(Blocks.WOOL, 11);
			final BlockObj farmland = new BlockObj(Blocks.FARMLAND, 0);
			final BlockObj cropBlock = new BlockObj(cropEntry.getCropBlock(), 0);
			final BlockObj waterBlock = new BlockObj(Blocks.WATER, 0);

			final Map<Point3D, BlockObj> changes = new HashMap<Point3D, BlockObj>();

			for (final Map.Entry<Point3D, BlockObj> entry : schematicMap.entrySet())
			{
				if (entry.getValue().equals(searchRefBlock))
				{
					final Point3D key = entry.getKey();
					final Point3D belowKey = RadixSchematics.getPoint3DWithValue(schematicMap, new Point3D(key.iX(), key.iY() - 1, key.iZ()));

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
	public String getName() 
	{
		return "Building";
	}
}

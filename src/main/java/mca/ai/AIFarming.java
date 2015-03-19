package mca.ai;

import java.util.Map;

import mca.api.ChoreRegistry;
import mca.api.CropEntry;
import mca.api.exception.MappingNotFoundException;
import mca.core.Constants;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.data.BlockObj;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;
import radixcore.util.SchematicHandler;

public class AIFarming extends AbstractToggleAI
{
	private static final int FARM_INTERVAL = Time.SECOND * 1;

	private WatchedBoolean isAIActive;
	private Point3D farmCenterPoint;
	private Point3D harvestTargetPoint;
	private int apiId;
	private int radius;
	private int activityInterval;
	private boolean doCreate;
	private boolean isBuildingFarm;
	private boolean farmCreatedFlag;
	private String schematic;

	public AIFarming(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_FARMING_ACTIVE, owner.getDataWatcherEx());
		farmCenterPoint = Point3D.ZERO;
		harvestTargetPoint = Point3D.ZERO;
		schematic = "none";
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
			if (activityInterval <= 0)
			{
				activityInterval = FARM_INTERVAL;

				if (doCreate)
				{
					if (!isBuildingFarm)
					{
						final CropEntry entry = ChoreRegistry.getCropEntryById(apiId);
						final int y = RadixLogic.getSpawnSafeTopLevel(owner.worldObj, (int) owner.posX, (int) owner.posZ);
						final Block groundBlock = owner.worldObj.getBlock((int)owner.posX, y - 1, (int)owner.posZ);
						owner.getAI(AIBuild.class).startBuilding(schematic, true, groundBlock, entry);

						isBuildingFarm = true;
					}

					else if (isBuildingFarm)
					{
						if (!owner.getAI(AIBuild.class).getIsActive())
						{
							isBuildingFarm = false;
							reset();
						}
					}
				}

				else //Harvest mode.
				{
					final double delta = RadixMath.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, harvestTargetPoint.dPosX, harvestTargetPoint.dPosY, harvestTargetPoint.dPosZ);

					if (harvestTargetPoint.iPosX == 0 && harvestTargetPoint.iPosY == 0 && harvestTargetPoint.iPosZ == 0)
					{
						for (int id : ChoreRegistry.getCropEntryIDs())
						{
							final CropEntry entry = ChoreRegistry.getCropEntryById(id);
							harvestTargetPoint = RadixLogic.getFirstNearestBlockWithMeta(owner, entry.getHarvestBlock(), entry.getHarvestBlockMeta(), radius);
							
							if (harvestTargetPoint == null)
							{
								harvestTargetPoint = Point3D.ZERO;
								continue;
							}

							else
							{
								apiId = id;
								break;
							}
						}
					}

					if (delta >= 2.0D && owner.getNavigator().noPath())
					{
						owner.getNavigator().tryMoveToXYZ(harvestTargetPoint.dPosX, harvestTargetPoint.dPosY, harvestTargetPoint.dPosZ, Constants.SPEED_WALK);
					}

					if (delta < 2.5D)
					{
						final CropEntry entry = ChoreRegistry.getCropEntryById(apiId);
						
						owner.swingItem();
						owner.worldObj.setBlock(harvestTargetPoint.iPosX, harvestTargetPoint.iPosY - 1, harvestTargetPoint.iPosZ, Blocks.farmland);
						owner.worldObj.setBlock(harvestTargetPoint.iPosX, harvestTargetPoint.iPosY, harvestTargetPoint.iPosZ, entry.getCropBlock());

						for (ItemStack stack : entry.getStacksOnHarvest())
						{
							if (stack != null)
							{
								owner.getInventory().addItemStackToInventory(stack);
							}
						}

						harvestTargetPoint = Point3D.ZERO;
					}

				}
			}

			activityInterval--;
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
		harvestTargetPoint.writeToNBT("harvestTargetPoint", nbt);
		nbt.setInteger("apiId", apiId);
		nbt.setInteger("farmingActivityInterval", activityInterval);
		nbt.setBoolean("farmCreatedFlag", farmCreatedFlag);
		nbt.setInteger("radius", radius);
		nbt.setBoolean("doCreate", doCreate);
		nbt.setString("schematic", schematic);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isFarmingActive"));
		farmCenterPoint = Point3D.readFromNBT("farmCenterPoint", nbt);
		harvestTargetPoint = Point3D.readFromNBT("harvestTargetPoint", nbt);
		apiId = nbt.getInteger("apiId");
		activityInterval = nbt.getInteger("farmingActivityInterval");
		farmCreatedFlag = nbt.getBoolean("farmCreatedFlag");
		radius = nbt.getInteger("radius");
		doCreate = nbt.getBoolean("doCreate");
		schematic = nbt.getString("schematic");
	}

	public void startFarming(EntityPlayer player, int apiId, int radius, boolean doCreate)
	{
		try
		{
			final CropEntry entry = ChoreRegistry.getCropEntryById(apiId);

			switch (entry.getCategory())
			{
			case WHEAT:	schematic = "/assets/mca/schematic/wheat1.schematic"; break;
			case MELON:	schematic = "/assets/mca/schematic/melon1.schematic"; break;
			case SUGARCANE:	schematic = "/assets/mca/schematic/sugarcane1.schematic"; break;
			}

			Map<Point3D, BlockObj> schematicData = SchematicHandler.readSchematic(schematic);
			int seedsRequired = SchematicHandler.countOccurencesOfBlockObj(schematicData, new BlockObj(Blocks.wool, entry.getCategory().getReferenceMeta()));

			if (doCreate && !owner.getInventory().containsCountOf(entry.getSeedItem(), seedsRequired))
			{
				owner.say("farming.noseeds", player, entry.getCropName().toLowerCase(), seedsRequired);
				return;
			}

			else if (player != null && !owner.getInventory().contains(ItemHoe.class))
			{
				owner.say("farming.nohoe", player);
				return;
			}

			//Assign arguments.
			this.assigningPlayer = player != null ? player.getUniqueID().toString() : "none";
			this.apiId = apiId;
			this.radius = radius;
			this.doCreate = doCreate;
			this.farmCenterPoint = new Point3D(owner.posX, owner.posY, owner.posZ);
			this.harvestTargetPoint = Point3D.ZERO;
			this.farmCreatedFlag = false;
			this.isBuildingFarm = false;
			this.isAIActive.setValue(true);

			if (seedsRequired > 0)
			{
				owner.getInventory().removeCountOfItem(entry.getSeedItem(), seedsRequired);
			}
		}

		catch (MappingNotFoundException e)
		{
			reset();
		}
	}
}

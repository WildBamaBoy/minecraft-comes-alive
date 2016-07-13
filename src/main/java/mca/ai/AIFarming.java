package mca.ai;

import java.util.Map;

import mca.api.CropEntry;
import mca.api.RegistryMCA;
import mca.api.enums.EnumCropCategory;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.data.BlockObj;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
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
	private int seedsRequired;
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
			if (!MCA.getConfig().allowFarmingChore)
			{
				this.notifyAssigningPlayer(Color.RED + "This chore is disabled.");
				reset();
				return;
			}
			
			if (activityInterval <= 0)
			{
				activityInterval = FARM_INTERVAL;

				if (doCreate)
				{
					if (!isBuildingFarm)
					{
						final CropEntry entry = RegistryMCA.getCropEntryById(apiId);
						final int y = RadixLogic.getSpawnSafeTopLevel(owner.worldObj, (int) owner.posX, (int) owner.posZ);
						Block groundBlock = BlockHelper.getBlock(owner.worldObj, (int)owner.posX, y - 1, (int)owner.posZ);

						if (groundBlock != Blocks.grass || groundBlock != Blocks.sand || groundBlock != Blocks.dirt)
						{
							groundBlock = Blocks.grass;
						}

						boolean canStart = owner.getAI(AIBuild.class).startBuilding(schematic, true, groundBlock, entry);

						if (canStart)
						{
							owner.getInventory().removeCountOfItem(entry.getSeedItem(), seedsRequired);
						}

						else
						{
							EntityPlayer assigningPlayer = this.getAssigningPlayer();

							if (assigningPlayer != null)
							{
								owner.say("build.fail.tooclose", assigningPlayer);
							}

							reset();
							return;
						}

						isBuildingFarm = true;
					}

					else if (isBuildingFarm)
					{
						if (!owner.getAI(AIBuild.class).getIsActive())
						{
							owner.damageHeldItem(30);
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
						for (int id : RegistryMCA.getCropEntryIDs())
						{
							final CropEntry entry = RegistryMCA.getCropEntryById(id);

							if (entry.getCategory() == EnumCropCategory.SUGARCANE)
							{
								Point3D nearestHarvest = RadixLogic.getFirstNearestBlockWithMeta(owner, entry.getHarvestBlock(), entry.getHarvestBlockMeta(), radius);

								if (nearestHarvest == null)
								{
									harvestTargetPoint = Point3D.ZERO;
									continue;
								}

								else
								{
									int yMod = 0;

									//Move y down until ground is found.
									while (BlockHelper.getBlock(owner.worldObj, nearestHarvest.iPosX, nearestHarvest.iPosY + yMod, nearestHarvest.iPosZ) != Blocks.grass 
											&& BlockHelper.getBlock(owner.worldObj, nearestHarvest.iPosX, nearestHarvest.iPosY + yMod, nearestHarvest.iPosZ) != Blocks.dirt)
									{
										yMod--;

										if (yMod < -10) //Avoid any potential of an infinite loop.
										{
											reset();
											break;
										}
									}

									//Bump up the harvest point by two, since the harvestable crop should be two above the ground block.
									Point3D modHarvestPoint = new Point3D(nearestHarvest.iPosX, nearestHarvest.iPosY + yMod + 2, nearestHarvest.iPosZ);
									
									//Make sure the harvest block is there, then assign the harvest point.
									if (BlockHelper.getBlock(owner.worldObj, modHarvestPoint.iPosX, modHarvestPoint.iPosY, modHarvestPoint.iPosZ) == entry.getHarvestBlock())
									{
										harvestTargetPoint = modHarvestPoint;
										apiId = id;
										break;
									}
									
									else
									{
										harvestTargetPoint = Point3D.ZERO;
										continue;
									}
								}
							}

							else
							{
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
					}

					if (delta >= 2.0D && owner.getNavigator().noPath())
					{
						owner.getNavigator().tryMoveToXYZ(harvestTargetPoint.dPosX, harvestTargetPoint.dPosY, harvestTargetPoint.dPosZ, owner.getSpeed());
					}

					if (delta < 2.5D)
					{
						final CropEntry entry = RegistryMCA.getCropEntryById(apiId);

						owner.swingItem();
						owner.damageHeldItem(2);
						
						if (entry.getCategory() == EnumCropCategory.WHEAT)
						{
							BlockHelper.setBlock(owner.worldObj, harvestTargetPoint.iPosX, harvestTargetPoint.iPosY - 1, harvestTargetPoint.iPosZ, Blocks.farmland);
							BlockHelper.setBlock(owner.worldObj, harvestTargetPoint.iPosX, harvestTargetPoint.iPosY, harvestTargetPoint.iPosZ, entry.getCropBlock());
						}
						
						else
						{
							BlockHelper.setBlock(owner.worldObj, harvestTargetPoint.iPosX, harvestTargetPoint.iPosY, harvestTargetPoint.iPosZ, Blocks.air);
						}
						
						for (ItemStack stack : entry.getStacksOnHarvest())
						{
							if (stack != null)
							{
								addItemStackToInventory(stack);
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
		seedsRequired = 0;
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
			final CropEntry entry = RegistryMCA.getCropEntryById(apiId);

			switch (entry.getCategory())
			{
			case WHEAT:	schematic = "/assets/mca/schematic/wheat1.schematic"; break;
			case MELON:	schematic = "/assets/mca/schematic/melon1.schematic"; break;
			case SUGARCANE:	schematic = "/assets/mca/schematic/sugarcane1.schematic"; break;
			}

			Map<Point3D, BlockObj> schematicData = SchematicHandler.readSchematic(schematic);
			seedsRequired = SchematicHandler.countOccurencesOfBlockObj(schematicData, new BlockObj(Blocks.wool, entry.getCategory().getReferenceMeta()));

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
			this.activityInterval = 0;
			this.apiId = apiId;
			this.radius = radius;
			this.doCreate = doCreate;
			this.farmCenterPoint = new Point3D(owner.posX, owner.posY, owner.posZ);
			this.harvestTargetPoint = Point3D.ZERO;
			this.farmCreatedFlag = false;
			this.isBuildingFarm = false;
			this.isAIActive.setValue(true);
			
			owner.setHeldItem(owner.getInventory().getBestItemOfType(ItemHoe.class).getItem());
		}

		catch (MappingNotFoundException e)
		{
			reset();
		}
	}
	
	@Override
	protected String getName() 
	{
		return "Farming";
	}
}

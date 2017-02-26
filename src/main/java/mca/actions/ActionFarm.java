package mca.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mca.api.CropEntry;
import mca.api.RegistryMCA;
import mca.api.enums.EnumCropCategory;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.schematics.BlockObj;
import radixcore.modules.schematics.RadixSchematics;

public class ActionFarm extends AbstractToggleAction
{
	private static final int FARM_INTERVAL = Time.SECOND * 1;

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

	public ActionFarm(EntityVillagerMCA actor) 
	{
		super(actor);
		farmCenterPoint = Point3D.ZERO;
		harvestTargetPoint = Point3D.ZERO;
		schematic = "none";
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
						final int y = RadixLogic.getSpawnSafeTopLevel(actor.world, (int) actor.posX, (int) actor.posZ);
						Block groundBlock = RadixBlocks.getBlock(actor.world, (int)actor.posX, y - 1, (int)actor.posZ);

						if (groundBlock != Blocks.GRASS || groundBlock != Blocks.SAND || groundBlock != Blocks.DIRT)
						{
							groundBlock = Blocks.GRASS;
						}

						boolean canStart = actor.getBehavior(ActionBuild.class).startBuilding(schematic, true, groundBlock, entry);

						if (canStart)
						{
							actor.attributes.getInventory().removeCountOfItem(entry.getSeedItem(), seedsRequired);
						}

						else
						{
							EntityPlayer assigningPlayerInst = this.getAssigningPlayer();

							if (assigningPlayerInst != null)
							{
								actor.say("build.fail.tooclose", assigningPlayerInst);
							}

							reset();
							return;
						}

						isBuildingFarm = true;
					}

					else if (isBuildingFarm)
					{
						if (!actor.getBehavior(ActionBuild.class).getIsActive())
						{
							actor.damageHeldItem(30);
							isBuildingFarm = false;
							reset();
						}
					}
				}

				else //Harvest mode.
				{
					final double delta = RadixMath.getDistanceToXYZ(actor.posX, actor.posY, actor.posZ, harvestTargetPoint.dX(), harvestTargetPoint.dY(), harvestTargetPoint.dZ());

					if (harvestTargetPoint.iX() == 0 && harvestTargetPoint.iY() == 0 && harvestTargetPoint.iZ() == 0)
					{
						for (int id : RegistryMCA.getCropEntryIDs())
						{
							final CropEntry entry = RegistryMCA.getCropEntryById(id);
							
							if (entry.getCategory() == EnumCropCategory.SUGARCANE)
							{
								Point3D nearestHarvest = RadixLogic.getFirstNearestBlockWithMeta(actor, entry.getHarvestBlock(), entry.getHarvestBlockMeta(), radius);

								if (nearestHarvest == null)
								{
									harvestTargetPoint = Point3D.ZERO;
									continue;
								}

								else
								{
									int yMod = 0;

									//Move y down until ground is found.
									while (RadixBlocks.getBlock(actor.world, nearestHarvest.iX(), nearestHarvest.iY() + yMod, nearestHarvest.iZ()) != Blocks.GRASS 
											&& RadixBlocks.getBlock(actor.world, nearestHarvest.iX(), nearestHarvest.iY() + yMod, nearestHarvest.iZ()) != Blocks.DIRT)
									{
										yMod--;

										if (yMod < -10) //Avoid any potential of an infinite loop.
										{
											reset();
											break;
										}
									}

									//Bump up the harvest point by two, since the harvestable crop should be two above the ground block.
									Point3D modHarvestPoint = new Point3D(nearestHarvest.iX(), nearestHarvest.iY() + yMod + 2, nearestHarvest.iZ());
									
									//Make sure the harvest block is there, then assign the harvest point.
									if (RadixBlocks.getBlock(actor.world, modHarvestPoint.iX(), modHarvestPoint.iY(), modHarvestPoint.iZ()) == entry.getHarvestBlock())
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
								List<Point3D> nearbyBlocks = RadixLogic.getNearbyBlocks(actor, entry.getHarvestBlock(), radius);
								List<Point3D> validMetaBlocks = new ArrayList<Point3D>();
								
								for (Point3D point : nearbyBlocks)
								{
									IBlockState state = actor.world.getBlockState(new BlockPos(point.iX(), point.iY(), point.iZ()));
									int meta = state.getBlock().getMetaFromState(state);

									if (meta == entry.getHarvestBlockMeta())
									{
										validMetaBlocks.add(point);
									}
								}
								
								double distance = 100.0D;
								
								for (Point3D point : validMetaBlocks)
								{
									double distanceToPoint = RadixMath.getDistanceToXYZ(actor, point);
									
									if (distanceToPoint < distance)
									{
										distance = distanceToPoint;
										harvestTargetPoint = point;
									}
								}
								
								if (harvestTargetPoint == null || harvestTargetPoint == Point3D.ZERO)
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

					if (delta >= 2.0D && actor.getNavigator().noPath())
					{
						actor.getNavigator().tryMoveToXYZ(harvestTargetPoint.dX(), harvestTargetPoint.dY(), harvestTargetPoint.dZ(), actor.attributes.getSpeed());
					}

					if (delta < 2.5D)
					{
						final CropEntry entry = RegistryMCA.getCropEntryById(apiId);

						actor.swingItem();
						actor.damageHeldItem(2);
						
						if (entry.getCategory() == EnumCropCategory.WHEAT)
						{
							RadixBlocks.setBlock(actor.world, harvestTargetPoint.iX(), harvestTargetPoint.iY() - 1, harvestTargetPoint.iZ(), Blocks.FARMLAND);
							RadixBlocks.setBlock(actor.world, harvestTargetPoint.iX(), harvestTargetPoint.iY(), harvestTargetPoint.iZ(), entry.getCropBlock());
						}
						
						else
						{
							RadixBlocks.setBlock(actor.world, harvestTargetPoint.iX(), harvestTargetPoint.iY(), harvestTargetPoint.iZ(), Blocks.AIR);
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
		setIsActive(false);
		seedsRequired = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isFarmingActive", getIsActive());
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
		setIsActive(nbt.getBoolean("isFarmingActive"));
		farmCenterPoint = Point3D.readFromNBT("farmCenterPoint", nbt);
		harvestTargetPoint = Point3D.readFromNBT("harvestTargetPoint", nbt);
		apiId = nbt.getInteger("apiId");
		activityInterval = nbt.getInteger("farmingActivityInterval");
		farmCreatedFlag = nbt.getBoolean("farmCreatedFlag");
		radius = nbt.getInteger("radius");
		doCreate = nbt.getBoolean("doCreate");
		schematic = nbt.getString("schematic");
	}

	public void startFarming(EntityPlayer player, int apiId1, int radius1, boolean doCreate1)
	{
		try
		{
			final CropEntry entry = RegistryMCA.getCropEntryById(apiId1);

			switch (entry.getCategory())
			{
			case WHEAT:	schematic = "/assets/mca/schematic/wheat1.schematic"; break;
			case MELON:	schematic = "/assets/mca/schematic/melon1.schematic"; break;
			case SUGARCANE:	schematic = "/assets/mca/schematic/sugarcane1.schematic"; break;
			}

			Map<Point3D, BlockObj> schematicData = RadixSchematics.readSchematic(schematic);
			seedsRequired = RadixSchematics.countOccurencesOfBlockObj(schematicData, new BlockObj(Blocks.WOOL, entry.getCategory().getReferenceMeta()));

			if (doCreate1 && !actor.attributes.getInventory().containsCountOf(entry.getSeedItem(), seedsRequired))
			{
				actor.say("farming.noseeds", player, entry.getCropName().toLowerCase(), seedsRequired);
				return;
			}

			else if (player != null && !actor.attributes.getInventory().contains(ItemHoe.class))
			{
				actor.say("farming.nohoe", player);
				return;
			}

			//Assign arguments.
			this.assigningPlayer = player != null ? player.getUniqueID() : new UUID(0, 0);
			this.activityInterval = 0;
			this.apiId = apiId1;
			this.radius = radius1;
			this.doCreate = doCreate1;
			this.farmCenterPoint = new Point3D(actor.posX, actor.posY, actor.posZ);
			this.harvestTargetPoint = Point3D.ZERO;
			this.farmCreatedFlag = false;
			this.isBuildingFarm = false;
			this.setIsActive(true);
			
			actor.setHeldItem(actor.attributes.getInventory().getBestItemOfType(ItemHoe.class).getItem());
		}

		catch (MappingNotFoundException e)
		{
			reset();
		}
	}
	
	@Override
	public String getName() 
	{
		return "Farming";
	}
}

package mca.ai;

import mca.api.RegistryMCA;
import mca.api.WoodcuttingEntry;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumPersonality;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import radixcore.constant.Font.Color;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIWoodcutting extends AbstractToggleAI
{
	private WatchedBoolean isAIActive;
	private Point3D treeBasePoint;
	private int apiId;
	private int yLevel;
	private int cutInterval;
	private int cutTimeLeft;
	private boolean doReplant;

	public AIWoodcutting(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_WOODCUTTING_ACTIVE, owner.getDataWatcherEx());
		treeBasePoint = Point3D.ZERO;
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
			if (!MCA.getConfig().allowWoodcuttingChore)
			{
				this.notifyAssigningPlayer(Color.RED + "This chore is disabled.");
				reset();
				return;
			}

			if (treeBasePoint.iPosX == 0 && treeBasePoint.iPosY == 0 && treeBasePoint.iPosZ == 0)
			{
				final WoodcuttingEntry apiEntry = RegistryMCA.getWoodcuttingEntryById(apiId);

				if (apiEntry.getLogBlock() == null) //Protect against possible NPE.
				{
					isAIActive.setValue(false);
					return;
				}

				final Point3D point = RadixLogic.getFirstNearestBlock(owner, apiEntry.getLogBlock(), 15);

				if (point != null)
				{
					//Follow the point down until logs are NOT found, so we have the base of the tree.
					while (BlockHelper.getBlock(owner.worldObj, point.iPosX, point.iPosY, point.iPosZ) == apiEntry.getLogBlock())
					{
						point.iPosY--;

						if (point.iPosY <= 0) //Impose a limit on failure
						{
							break;
						}
					}

					//Follow back up and make sure we have the base. Not sure why, simply adding 1 caused issues every now and then.
					while (BlockHelper.getBlock(owner.worldObj, point.iPosX, point.iPosY, point.iPosZ) != apiEntry.getLogBlock())
					{
						point.iPosY++;

						if (point.iPosY >= 255)
						{
							break;
						}
					}

					Point3D modifiedPoint = new Point3D(point.iPosX, point.iPosY, point.iPosZ);
					treeBasePoint = modifiedPoint;
				}

				else
				{
					notifyAssigningPlayer("There are no logs nearby.");
					isAIActive.setValue(false);
					return;
				}
			}

			else if (RadixMath.getDistanceToXYZ(treeBasePoint.dPosX, treeBasePoint.dPosY, treeBasePoint.dPosZ, owner.posX, owner.posY, owner.posZ) <= 2.5D || yLevel > 0)
			{
				cutTimeLeft--;
				owner.swingItem();

				if (cutTimeLeft <= 0)
				{
					cutTimeLeft = cutInterval;

					final WoodcuttingEntry apiEntry = RegistryMCA.getWoodcuttingEntryById(apiId);
					final Block block = apiEntry.getLogBlock();
					BlockHelper.setBlock(owner.worldObj, treeBasePoint.iPosX, treeBasePoint.iPosY + yLevel, treeBasePoint.iPosZ, Blocks.air);
					boolean addedToInventory = addItemStackToInventory(new ItemStack(block, 1, apiEntry.getLogMeta()));
					boolean toolBroken = owner.damageHeldItem(2);

					if (!addedToInventory && owner.getPersonality() == EnumPersonality.GREEDY)
					{
						//pass on greedy
					}

					else if (!addedToInventory)
					{
						notifyAssigningPlayer("My inventory is full.");
						isAIActive.setValue(false);
						return;
					}

					else if (toolBroken)
					{
						notifyAssigningPlayer("My axe has broken.");
						isAIActive.setValue(false);
						return;					
					}

					yLevel++;

					//Check that the next y level still contains a tree, reset if not.
					final Block nextBlock = BlockHelper.getBlock(owner.worldObj, treeBasePoint.iPosX, treeBasePoint.iPosY + yLevel, treeBasePoint.iPosZ);

					if (nextBlock != apiEntry.getLogBlock())
					{
						if (apiEntry.hasSapling() && doReplant)
						{
							BlockHelper.setBlock(owner.worldObj, treeBasePoint.iPosX, treeBasePoint.iPosY - 1, treeBasePoint.iPosZ, Blocks.dirt);
							BlockHelper.setBlock(owner.worldObj, treeBasePoint.iPosX, treeBasePoint.iPosY, treeBasePoint.iPosZ, apiEntry.getSaplingBlock());
						}

						yLevel = 0;
						treeBasePoint = Point3D.ZERO;
					}
				}
			}

			else
			{
				for (Point3D point : RadixLogic.getNearbyBlocks(owner, Blocks.leaves, 1))
				{
					BlockHelper.setBlock(owner.worldObj, point.iPosX, point.iPosY, point.iPosZ, Blocks.air);
				}

				for (Point3D point : RadixLogic.getNearbyBlocks(owner, Blocks.leaves2, 1))
				{
					BlockHelper.setBlock(owner.worldObj, point.iPosX, point.iPosY, point.iPosZ, Blocks.air);				
				}

				if (owner.getNavigator().noPath())
				{
					owner.getNavigator().tryMoveToXYZ(treeBasePoint.dPosX, treeBasePoint.dPosY, treeBasePoint.dPosZ, owner.getSpeed());
				}
			}
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
		nbt.setBoolean("isWoodcuttingActive", isAIActive.getBoolean());

		treeBasePoint.writeToNBT("treeBasePoint", nbt);
		nbt.setInteger("apiId", apiId);
		nbt.setInteger("yLevel", yLevel);
		nbt.setInteger("cutInterval", cutInterval);
		nbt.setInteger("cutTimeLeft", cutTimeLeft);
		nbt.setBoolean("doReplant", doReplant);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isWoodcuttingActive"));

		treeBasePoint = Point3D.readFromNBT("treeBasePoint", nbt);
		apiId = nbt.getInteger("apiId");
		yLevel = nbt.getInteger("yLevel");
		cutInterval = nbt.getInteger("cutInterval");
		cutTimeLeft = nbt.getInteger("cutTimeLeft");
		doReplant = nbt.getBoolean("doReplant");
	}

	public void startWoodcutting(EntityPlayer player, int apiId, boolean doReplant)
	{
		this.apiId = apiId;
		this.assigningPlayer = player.getUniqueID().toString();
		this.yLevel = 0;
		this.doReplant = doReplant;
		this.cutInterval = calculateCutInterval();		
		this.cutTimeLeft = cutInterval;

		this.isAIActive.setValue(true);
	}

	private int calculateCutInterval()
	{
		ItemStack bestAxe = owner.getVillagerInventory().getBestItemOfType(ItemAxe.class);
		int returnAmount = -1;

		if (bestAxe != null)
		{
			Item item = bestAxe.getItem();
			ToolMaterial material = ToolMaterial.valueOf(((ItemAxe) bestAxe.getItem()).getToolMaterialName());	

			switch (material)
			{
			case WOOD:
				returnAmount = 40;
				break;
			case STONE:
				returnAmount = 30;
				break;
			case IRON:
				returnAmount = 25;
				break;
			case EMERALD:
				returnAmount = 10;
				break;
			case GOLD:
				returnAmount = 5;
				break;
			default:
				returnAmount = 25;
				break;
			}

			owner.setHeldItem(item);
		}

		else
		{
			returnAmount = 60;
			owner.setHeldItem(null);
		}

		return returnAmount;
	}

	@Override
	protected String getName() 
	{
		return "Woodcutting";
	}
}

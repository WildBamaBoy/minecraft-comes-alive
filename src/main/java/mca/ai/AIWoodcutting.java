package mca.ai;

import mca.api.RegistryMCA;
import mca.api.WoodcuttingEntry;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumPersonality;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Font.Color;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.datawatcher.WatchedBoolean;

public class AIWoodcutting extends AbstractToggleAI
{
	private WatchedBoolean isAIActive;
	private Point3D treeBasePoint;
	private int apiId;
	private int yLevel;
	private int cutInterval;
	private int cutTimeLeft;
	private boolean doReplant;

	public AIWoodcutting(EntityVillagerMCA owner) 
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

			if (treeBasePoint.iX() == 0 && treeBasePoint.iY() == 0 && treeBasePoint.iZ() == 0)
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
					while (RadixBlocks.getBlock(owner.worldObj, point.iX(), point.iY(), point.iZ()) == apiEntry.getLogBlock())
					{
						point.iY()--;

						if (point.iY() <= 0) //Impose a limit on failure
						{
							break;
						}
					}

					//Follow back up and make sure we have the base. Not sure why, simply adding 1 caused issues every now and then.
					while (RadixBlocks.getBlock(owner.worldObj, point.iX(), point.iY(), point.iZ()) != apiEntry.getLogBlock())
					{
						point.iY()++;

						if (point.iY() >= 255)
						{
							break;
						}
					}

					Point3D modifiedPoint = new Point3D(point.iX(), point.iY(), point.iZ());
					treeBasePoint = modifiedPoint;
				}

				else
				{
					notifyAssigningPlayer("There are no logs nearby.");
					isAIActive.setValue(false);
					return;
				}
			}

			else if (RadixMath.getDistanceToXYZ(treeBasePoint.dX(), treeBasePoint.dY(), treeBasePoint.dZ(), owner.posX, owner.posY, owner.posZ) <= 2.5D || yLevel > 0)
			{
				cutTimeLeft--;
				owner.swingItem();

				if (cutTimeLeft <= 0)
				{
					cutTimeLeft = cutInterval;

					final WoodcuttingEntry apiEntry = RegistryMCA.getWoodcuttingEntryById(apiId);
					final Block block = apiEntry.getLogBlock();
					RadixBlocks.setBlock(owner.worldObj, treeBasePoint.iX(), treeBasePoint.iY() + yLevel, treeBasePoint.iZ(), Blocks.AIR);
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
					final Block nextBlock = RadixBlocks.getBlock(owner.worldObj, treeBasePoint.iX(), treeBasePoint.iY() + yLevel, treeBasePoint.iZ());

					if (nextBlock != apiEntry.getLogBlock())
					{
						if (apiEntry.hasSapling() && doReplant)
						{
							RadixBlocks.setBlock(owner.worldObj, treeBasePoint.iX(), treeBasePoint.iY() - 1, treeBasePoint.iZ(), Blocks.DIRT);
							RadixBlocks.setBlock(owner.worldObj, treeBasePoint.iX(), treeBasePoint.iY(), treeBasePoint.iZ(), apiEntry.getSaplingBlock());
						}

						yLevel = 0;
						treeBasePoint = Point3D.ZERO;
					}
				}
			}

			else
			{
				for (Point3D point : RadixLogic.getNearbyBlocks(owner, Blocks.LEAVES, 1))
				{
					RadixBlocks.setBlock(owner.worldObj, point.iX(), point.iY(), point.iZ(), Blocks.AIR);
				}

				for (Point3D point : RadixLogic.getNearbyBlocks(owner, Blocks.LEAVES2, 1))
				{
					RadixBlocks.setBlock(owner.worldObj, point.iX(), point.iY(), point.iZ(), Blocks.AIR);				
				}

				if (owner.getNavigator().noPath())
				{
					owner.getNavigator().tryMoveToXYZ(treeBasePoint.dX(), treeBasePoint.dY(), treeBasePoint.dZ(), owner.getSpeed());
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
			case DIAMOND:
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

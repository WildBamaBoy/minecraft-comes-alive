package mca.actions;

import mca.api.RegistryMCA;
import mca.api.WoodcuttingEntry;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
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

public class ActionWoodcut extends AbstractToggleAction
{
	private Point3D treeBasePoint;
	private int apiId;
	private int yLevel;
	private int cutInterval;
	private int cutTimeLeft;
	private boolean doReplant;

	public ActionWoodcut(EntityVillagerMCA actor) 
	{
		super(actor);
		treeBasePoint = Point3D.ZERO;
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
					setIsActive(false);
					return;
				}

				final Point3D point = RadixLogic.getNearestBlock(actor, 15, apiEntry.getLogBlock());

				if (point != null)
				{
					//Follow the point down until logs are NOT found, so we have the base of the tree.
					while (RadixBlocks.getBlock(actor.world, point.iX(), point.iY(), point.iZ()) == apiEntry.getLogBlock())
					{
						point.set(point.iX(), point.iY() - 1, point.iZ());

						if (point.iY() <= 0) //Impose a limit on failure
						{
							break;
						}
					}

					//Follow back up and make sure we have the base. Not sure why, simply adding 1 caused issues every now and then.
					while (RadixBlocks.getBlock(actor.world, point.iX(), point.iY(), point.iZ()) != apiEntry.getLogBlock())
					{
						point.set(point.iX(), point.iY() + 1, point.iZ());

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
					setIsActive(false);
					return;
				}
			}

			else if (RadixMath.getDistanceToXYZ(treeBasePoint.dX(), treeBasePoint.dY(), treeBasePoint.dZ(), actor.posX, actor.posY, actor.posZ) <= 2.5D || yLevel > 0)
			{
				cutTimeLeft--;
				actor.swingItem();

				if (cutTimeLeft <= 0)
				{
					cutTimeLeft = cutInterval;

					final WoodcuttingEntry apiEntry = RegistryMCA.getWoodcuttingEntryById(apiId);
					final Block block = apiEntry.getLogBlock();
					RadixBlocks.setBlock(actor.world, treeBasePoint.iX(), treeBasePoint.iY() + yLevel, treeBasePoint.iZ(), Blocks.AIR);
					boolean addedToInventory = addItemStackToInventory(new ItemStack(block, 1, apiEntry.getLogMeta()));
					boolean toolBroken = actor.damageHeldItem(2);

					if (!addedToInventory && actor.attributes.getPersonality() == EnumPersonality.GREEDY)
					{
						//pass on greedy
					}

					else if (!addedToInventory)
					{
						notifyAssigningPlayer("My inventory is full.");
						setIsActive(false);
						return;
					}

					else if (toolBroken)
					{
						notifyAssigningPlayer("My axe has broken.");
						setIsActive(false);
						return;					
					}

					yLevel++;

					//Check that the next y level still contains a tree, reset if not.
					final Block nextBlock = RadixBlocks.getBlock(actor.world, treeBasePoint.iX(), treeBasePoint.iY() + yLevel, treeBasePoint.iZ());

					if (nextBlock != apiEntry.getLogBlock())
					{
						if (apiEntry.hasSapling() && doReplant)
						{
							RadixBlocks.setBlock(actor.world, treeBasePoint.iX(), treeBasePoint.iY() - 1, treeBasePoint.iZ(), Blocks.DIRT);
							RadixBlocks.setBlock(actor.world, treeBasePoint.iX(), treeBasePoint.iY(), treeBasePoint.iZ(), apiEntry.getSaplingBlock());
						}

						yLevel = 0;
						treeBasePoint = Point3D.ZERO;
					}
				}
			}

			else
			{
				for (Point3D point : RadixLogic.getNearbyBlocks(actor, Blocks.LEAVES, 1))
				{
					RadixBlocks.setBlock(actor.world, point.iX(), point.iY(), point.iZ(), Blocks.AIR);
				}

				for (Point3D point : RadixLogic.getNearbyBlocks(actor, Blocks.LEAVES2, 1))
				{
					RadixBlocks.setBlock(actor.world, point.iX(), point.iY(), point.iZ(), Blocks.AIR);				
				}

				if (actor.getNavigator().noPath())
				{
					actor.getNavigator().tryMoveToXYZ(treeBasePoint.dX(), treeBasePoint.dY(), treeBasePoint.dZ(), actor.attributes.getSpeed());
				}
			}
		}

		catch (MappingNotFoundException e)
		{
			reset();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isWoodcuttingActive", getIsActive());

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
		setIsActive(nbt.getBoolean("isWoodcuttingActive"));

		treeBasePoint = Point3D.readFromNBT("treeBasePoint", nbt);
		apiId = nbt.getInteger("apiId");
		yLevel = nbt.getInteger("yLevel");
		cutInterval = nbt.getInteger("cutInterval");
		cutTimeLeft = nbt.getInteger("cutTimeLeft");
		doReplant = nbt.getBoolean("doReplant");
	}

	public void startWoodcutting(EntityPlayer player, int apiIdIn, boolean doReplantIn)
	{
		this.apiId = apiIdIn;
		this.assigningPlayer = player.getUniqueID();
		this.yLevel = 0;
		this.doReplant = doReplantIn;
		this.cutInterval = calculateCutInterval();		
		this.cutTimeLeft = cutInterval;

		setIsActive(true);
	}

	private int calculateCutInterval()
	{
		ItemStack bestAxe = actor.attributes.getInventory().getBestItemOfType(ItemAxe.class);
		int returnAmount = -1;

		if (bestAxe != ItemStack.EMPTY)
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

			actor.setHeldItem(item);
		}

		else
		{
			returnAmount = 60;
			actor.setHeldItem(null);
		}

		return returnAmount;
	}

	@Override
	public String getName() 
	{
		return "Woodcutting";
	}
}

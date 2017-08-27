package mca.actions;

import java.util.List;
import java.util.Map;

import mca.api.MiningEntry;
import mca.api.RegistryMCA;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionMine extends AbstractToggleAction
{
	private static final int SEARCH_INTERVAL = Time.SECOND * 10;
	private static final int MINE_INTERVAL = Time.SECOND * 1;

	private int idOfNotifyBlock;
	private int activityInterval;
	private boolean isGathering;
	private boolean isBuildingMine;

	public ActionMine(EntityVillagerMCA actor) 
	{
		super(actor);
		setIsActive(false);
	}
	
	@Override
	public void onUpdateServer() 
	{
		if (!MCA.getConfig().allowMiningChore)
		{
			this.notifyAssigningPlayer(Color.RED + "This chore is disabled.");
			reset();
			return;
		}
		
		if (isGathering)
		{
			runGather();
		}

		else //Searching
		{
			runSearch();
		}
	}

	private void runGather()
	{
		if (activityInterval <= 0)
		{
			activityInterval = MINE_INTERVAL;

			//If we're not already building and oak fence isn't found, begin building the mine.
			if (!isBuildingMine && RadixLogic.getNearbyBlocks(actor, Blocks.OAK_FENCE, 8).size() == 0)
			{
				final int y = RadixLogic.getSpawnSafeTopLevel(actor.world, (int) actor.posX, (int) actor.posZ);
				final Block groundBlock = RadixBlocks.getBlock(actor.world, (int)actor.posX, y - 1, (int)actor.posZ);
				actor.getBehavior(ActionBuild.class).startBuilding("/assets/mca/schematic/mine1.schematic", true, groundBlock);

				isBuildingMine = true;
			}

			//If build flag has been set, check to see if the build AI is still running.
			else if (isBuildingMine)
			{
				if (!actor.getBehavior(ActionBuild.class).getIsActive())
				{
					//When the chore is not running, search for a group of fences nearby.
					//This identifies this area as a mine.
					List<Point3D> nearbyFence = RadixLogic.getNearbyBlocks(actor, Blocks.OAK_FENCE_GATE, 8);

					if (nearbyFence.size() >= 1)
					{
						isBuildingMine = false;
					}

					else
					{
						reset();
					}
				}
			}

			else //Clear to continue mining.
			{
				actor.attributes.setMovementState(EnumMovementState.STAY);
				actor.swingItem();

				ItemStack addStack = getHarvestStack();

				if (addStack != ItemStack.EMPTY)
				{
					actor.attributes.getInventory().addItem(addStack);
					boolean pickBroken = actor.damageHeldItem(2);
					
					if (pickBroken && !getPickFromInventory())
					{
						actor.say("interaction.mining.fail.broken", this.getAssigningPlayer());
						reset();
					}
				}
			}
		}

		activityInterval--;
	}
	
	private void runSearch()
	{
		try
		{
			if (activityInterval <= 0)
			{
				activityInterval = SEARCH_INTERVAL;

				final Block notifyBlock = RegistryMCA.getMiningEntryById(idOfNotifyBlock).getBlock();
				final Point3D ownerPos = new Point3D(actor.posX, actor.posY, actor.posZ);
				int distanceToBlock = -1;

				//Find the nearest block we can notify about.
				for (final Point3D point : RadixLogic.getNearbyBlocks(actor, notifyBlock, 20))
				{
					if (distanceToBlock == -1)
					{
						distanceToBlock = (int) RadixMath.getDistanceToXYZ(point.iX(), point.iY(), point.iZ(), ownerPos.iX(), ownerPos.iY(), ownerPos.iZ());
					}

					else
					{
						double distanceToPoint = RadixMath.getDistanceToXYZ(point.iX(), point.iY(), point.iZ(), ownerPos.iX(), ownerPos.iY(), ownerPos.iZ());

						if (distanceToPoint < distanceToBlock)
						{
							distanceToBlock = (int) distanceToPoint;
						}
					}
				}

				//Determine which message we're going to use and the arguments.
				final String phraseId;
				Object[] arguments = new Object[2];

				if (distanceToBlock == -1)
				{
					phraseId = "mining.search.none";
					arguments[0] = notifyBlock.getLocalizedName().toLowerCase();
				}

				else if (distanceToBlock <= 5)
				{
					phraseId = "mining.search.nearby";
					arguments[0] = notifyBlock.getLocalizedName().toLowerCase();		
				}

				else
				{
					phraseId = "mining.search.value";
					arguments[0] = notifyBlock.getLocalizedName().toLowerCase();
					arguments[1] = distanceToBlock;
				}

				//Notify the player if they're on the server.
				final EntityPlayer player = actor.world.getPlayerEntityByUUID(assigningPlayer);

				if (player != null)
				{
					actor.say(phraseId, player, arguments);
				}
				
				//Damage the pick.
				boolean pickBroken = actor.damageHeldItem(5);
				
				//Notify for picks that have broken.
				if (pickBroken && !getPickFromInventory())
				{
					actor.say("interaction.mining.fail.broken", player);
					reset();
				}
			}

			activityInterval--;
		}

		catch (MappingNotFoundException e)
		{
			reset();
		}
	}
	
	private boolean getPickFromInventory()
	{
		ItemStack pickaxe = actor.attributes.getInventory().getBestItemOfType(ItemPickaxe.class);
		
		if (pickaxe != ItemStack.EMPTY)
		{
			actor.setHeldItem(pickaxe.getItem());
			return true;
		}
		
		return false;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isMiningActive", getIsActive());
		nbt.setInteger("idOfNotifyBlock", idOfNotifyBlock);
		nbt.setInteger("activityInterval", activityInterval);
		nbt.setUniqueId("assigningPlayer", assigningPlayer);
		nbt.setBoolean("isGathering", isGathering);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setIsActive(nbt.getBoolean("isMiningActive"));
		idOfNotifyBlock = nbt.getInteger("idOfNotifyBlock");
		activityInterval = nbt.getInteger("activityInterval");
		assigningPlayer = nbt.getUniqueId("assigningPlayer");
		isGathering = nbt.getBoolean("isGathering");
	}

	public void startSearching(EntityPlayer player, int notifyBlockId)
	{
		this.assigningPlayer = player.getPersistentID();
		this.idOfNotifyBlock = notifyBlockId;
		this.isGathering = false;
		this.setIsActive(true);
		this.activityInterval = SEARCH_INTERVAL;
		
		boolean hasPick = getPickFromInventory();
		
		if (!hasPick)
		{
			actor.say("interaction.mining.fail.nopickaxe", player);
			reset();
		}
	}

	public void reset()
	{
		this.setIsActive(false);
		this.activityInterval = 0;
		this.assigningPlayer = null;
		this.isGathering = false;
		this.isBuildingMine = false;
	}
	
	public void startGathering(EntityPlayer player) 
	{
		if (actor.posY <= 12)
		{
			actor.say("interaction.mining.fail.toolow", player);
			return;
		}

		this.assigningPlayer = player.getPersistentID();
		this.isGathering = true;
		this.setIsActive(true);
		this.activityInterval = 0;
		
		boolean hasPick = getPickFromInventory();
		
		if (!hasPick)
		{
			actor.say("interaction.mining.fail.nopickaxe", player);
			reset();
		}
	}
	
	@Override
	public String getName() 
	{
		return "Mining";
	}
	
	public ItemStack getHarvestStack()
	{
		boolean doHarvest = RadixLogic.getBooleanWithProbability(25);

		if (doHarvest)
		{
			ItemStack addStack = null;
			boolean getSpecialOre = RadixLogic.getBooleanWithProbability(3);

			if (getSpecialOre)
			{
				Map<Integer, MiningEntry> entries = RegistryMCA.getMiningEntryMap();
				float totalWeight = 0.0F;
				int index = -1;
				
				//Sum up the total weight of all entries.
				for (MiningEntry entry : entries.values())
				{
					totalWeight += entry.getWeight();
				}
				
				//Apply randomness.
				float random = (float) (Math.random() * totalWeight);
				
				// Subtract the weight of each item until we are at or less than zero. That entry
				// is the one we add.
				for (Map.Entry<Integer, MiningEntry> entry : entries.entrySet())
				{
					random -= entry.getValue().getWeight();
					
					if (random <= 0.0F)
					{
						index = entry.getKey();
						break;
					}
				}
				
				addStack = entries.get(index).getMinedItemStack();
			}

			else
			{
				addStack = new ItemStack(Blocks.COBBLESTONE, 1);
			}
			
			return addStack;
		}
		
		return ItemStack.EMPTY;
	}
}

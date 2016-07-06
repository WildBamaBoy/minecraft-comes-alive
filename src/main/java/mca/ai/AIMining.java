package mca.ai;

import java.util.List;
import java.util.Map;

import mca.api.MiningEntry;
import mca.api.RegistryMCA;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIMining extends AbstractToggleAI
{
	private static final int SEARCH_INTERVAL = Time.SECOND * 10;
	private static final int MINE_INTERVAL = Time.SECOND * 1;

	private WatchedBoolean isAIActive;
	private Point3D blockMining;

	private int idOfNotifyBlock;
	private int activityInterval;
	private boolean isGathering;
	private boolean isBuildingMine;

	public AIMining(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_MINING_ACTIVE, owner.getDataWatcherEx());
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
		if (!MCA.getConfig().allowMiningChore)
		{
			this.notifyAssigningPlayer(Color.RED + "This chore is disabled.");
			reset();
			return;
		}
		
		if (isGathering)
		{
			if (activityInterval <= 0)
			{
				activityInterval = MINE_INTERVAL;

				//If we're not already building and a stone block with a meta of 11 isn't found, begin building the mine.
				if (!isBuildingMine && RadixLogic.getNearbyBlocks(owner, Blocks.OAK_FENCE_GATE, 8).size() == 0)
				{
					final int y = RadixLogic.getSpawnSafeTopLevel(owner.worldObj, (int) owner.posX, (int) owner.posZ);
					final Block groundBlock = BlockHelper.getBlock(owner.worldObj, (int)owner.posX, y - 1, (int)owner.posZ);
					owner.getAI(AIBuild.class).startBuilding("/assets/mca/schematic/mine1.schematic", true, groundBlock);

					isBuildingMine = true;
				}

				//If build flag has been set, check to see if the build AI is still running.
				else if (isBuildingMine)
				{
					if (!owner.getAI(AIBuild.class).getIsActive())
					{
						//When the chore is not running, search for a group of fences nearby.
						//This identifies this area as a mine.
						List<Point3D> nearbyFence = RadixLogic.getNearbyBlocks(owner, Blocks.OAK_FENCE_GATE, 8);

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
					owner.setMovementState(EnumMovementState.STAY);
					owner.swingItem();

					ItemStack addStack = getHarvestStack();

					if (addStack != null)
					{
						owner.getVillagerInventory().addItemStackToInventory(addStack);
						owner.damageHeldItem(2);
					}
				}
			}

			activityInterval--;
		}

		else //Searching
		{
			try
			{
				if (activityInterval <= 0)
				{
					activityInterval = SEARCH_INTERVAL;

					final Block notifyBlock = RegistryMCA.getMiningEntryById(idOfNotifyBlock).getBlock();
					final Point3D ownerPos = new Point3D(owner.posX, owner.posY, owner.posZ);
					int distanceToBlock = -1;

					//Find the nearest block we can notify about.
					for (final Point3D point : RadixLogic.getNearbyBlocks(owner, notifyBlock, 20))
					{
						if (distanceToBlock == -1)
						{
							distanceToBlock = (int) RadixMath.getDistanceToXYZ(point.iPosX, point.iPosY, point.iPosZ, ownerPos.iPosX, ownerPos.iPosY, ownerPos.iPosZ);
						}

						else
						{
							double distanceToPoint = RadixMath.getDistanceToXYZ(point.iPosX, point.iPosY, point.iPosZ, ownerPos.iPosX, ownerPos.iPosY, ownerPos.iPosZ);

							if (distanceToPoint < distanceToBlock)
							{
								distanceToBlock = (int) distanceToPoint;
							}
						}
					}

					//Damage the pick.
					owner.damageHeldItem(5);

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
					final EntityPlayer player = RadixLogic.getPlayerByUUID(assigningPlayer, owner.worldObj);

					if (player != null)
					{
						owner.say(phraseId, player, arguments);
					}
				}

				activityInterval--;
			}

			catch (MappingNotFoundException e)
			{
				reset();
			}
		}
	}

	@Override
	public void reset() 
	{
		isAIActive.setValue(false);
		new Throwable().printStackTrace();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isMiningActive", isAIActive.getBoolean());
		nbt.setInteger("idOfNotifyBlock", idOfNotifyBlock);
		nbt.setInteger("activityInterval", activityInterval);
		nbt.setString("assigningPlayer", assigningPlayer);
		nbt.setBoolean("isGathering", isGathering);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isMiningActive"));
		idOfNotifyBlock = nbt.getInteger("idOfNotifyBlock");
		activityInterval = nbt.getInteger("activityInterval");
		assigningPlayer = nbt.getString("assigningPlayer");
		isGathering = nbt.getBoolean("isGathering");
	}

	public void startSearching(EntityPlayer assigningPlayer, int notifyBlockId)
	{
		this.assigningPlayer = assigningPlayer.getPersistentID().toString();
		this.idOfNotifyBlock = notifyBlockId;
		this.isGathering = false;
		this.isAIActive.setValue(true);
		this.activityInterval = SEARCH_INTERVAL;
		
		ItemStack pickaxe = owner.getVillagerInventory().getBestItemOfType(ItemPickaxe.class);
		
		if (pickaxe != null)
		{
			owner.setHeldItem(pickaxe.getItem());
		}
		
		else
		{
			owner.say("interaction.mining.fail.nopickaxe", assigningPlayer);
			reset();
		}
	}

	public void startGathering(EntityPlayer player) 
	{
		if (owner.posY <= 12)
		{
			owner.say("interaction.mining.fail.toolow", player);
			return;
		}

		this.assigningPlayer = player.getPersistentID().toString();
		this.isGathering = true;
		this.isAIActive.setValue(true);
		this.activityInterval = 0;
		
		ItemStack pickaxe = owner.getVillagerInventory().getBestItemOfType(ItemPickaxe.class);
		
		if (pickaxe != null)
		{
			owner.setHeldItem(pickaxe.getItem());
		}
		
		else
		{
			owner.say("interaction.mining.fail.nopickaxe", player);
			reset();
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
		
		return null;
	}
}

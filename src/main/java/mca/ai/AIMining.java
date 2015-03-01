package mca.ai;

import java.util.List;

import mca.api.ChoreRegistry;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;
import radixcore.math.Point3D;

/**
 * Two types:
 * Gather - Will gather blocks from nearby.
 * 
 * Search - Will search for blocks nearby and let the player know where they are.
 */
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
		if (isGathering)
		{
			if (activityInterval <= 0)
			{
				activityInterval = MINE_INTERVAL;
				
				//If we're not already building and a stone block with a meta of 11 isn't found, begin building the mine.
				if (!isBuildingMine && LogicHelper.getNearbyBlocksWithMetadata(owner, Blocks.stone, 11, 10).size() == 0)
				{
					final int y = LogicHelper.getSpawnSafeTopLevel(owner.worldObj, (int) owner.posX, (int) owner.posZ);
					final Block groundBlock = owner.worldObj.getBlock((int)owner.posX, y - 1, (int)owner.posZ);
					owner.getAI(AIBuild.class).startBuilding("/assets/mca/schematic/mine1.schematic", true, groundBlock);

					isBuildingMine = true;
				}
				
				//If build flag has been set, check to see if the build AI is still running.
				else if (isBuildingMine)
				{
					if (!owner.getAI(AIBuild.class).getIsActive())
					{
						//When the chore is not running, pick a random stone block nearby and set its meta to 11.
						//This identifies this area as a mine.
						List<Point3D> nearbyStone = LogicHelper.getNearbyBlocks(owner, Blocks.stone, 4);
						Point3D point = nearbyStone.get(MathHelper.getNumberInRange(0, nearbyStone.size()));
						
						owner.worldObj.setBlockMetadataWithNotify(point.iPosX, point.iPosY, point.iPosZ, 11, 2);
						isBuildingMine = false;
					}
				}
				
				else //Clear to continue mining.
				{
					owner.setMovementState(EnumMovementState.STAY);
					owner.swingItem();
				}
			}
			
			activityInterval--;
		}

		else //Searching
		{
			if (activityInterval <= 0)
			{
				activityInterval = SEARCH_INTERVAL;

				final Block notifyBlock = ChoreRegistry.getNotifyBlockById(idOfNotifyBlock);
				final Point3D ownerPos = new Point3D(owner.posX, owner.posY, owner.posZ);
				int distanceToBlock = -1;

				if (notifyBlock == null) //Error-proofing just in case the current ID no longer exists.
				{
					reset();
					return;
				}

				//Find the nearest block we can notify about.
				for (final Point3D point : LogicHelper.getNearbyBlocks(owner, notifyBlock, 20))
				{
					if (distanceToBlock == -1)
					{
						distanceToBlock = (int) MathHelper.getDistanceToXYZ(point.iPosX, point.iPosY, point.iPosZ, ownerPos.iPosX, ownerPos.iPosY, ownerPos.iPosZ);
					}

					else
					{
						double distanceToPoint = MathHelper.getDistanceToXYZ(point.iPosX, point.iPosY, point.iPosZ, ownerPos.iPosX, ownerPos.iPosY, ownerPos.iPosZ);

						if (distanceToPoint < distanceToBlock)
						{
							distanceToBlock = (int) distanceToPoint;
						}
					}
				}

				//Damage the pick.
				//TODO
				
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
				final EntityPlayer player = LogicHelper.getPlayerByUUID(assigningPlayer, owner.worldObj);

				if (player != null)
				{
					owner.say(MCA.getLanguageManager().getString(phraseId, arguments), player);
				}
			}

			activityInterval--;
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
		nbt.setBoolean("isAIActive", isAIActive.getBoolean());
		nbt.setInteger("idOfNotifyBlock", idOfNotifyBlock);
		nbt.setInteger("activityInterval", activityInterval);
		nbt.setString("assigningPlayer", assigningPlayer);
		nbt.setBoolean("isGathering", isGathering);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isAIActive"));
		idOfNotifyBlock = nbt.getInteger("idOfNotifyBlock");
		activityInterval = nbt.getInteger("activityInterval");
		assigningPlayer = nbt.getString("assigningPlayer");
		isGathering = nbt.getBoolean("isGathering");
	}

	public void startSearching(EntityPlayer assigningPlayer, Block searchBlock)
	{
		this.assigningPlayer = assigningPlayer.getPersistentID().toString();
		this.idOfNotifyBlock = ChoreRegistry.getIdOfNotifyBlock(searchBlock);
		this.isGathering = false;
		this.isAIActive.setValue(true);
		this.activityInterval = SEARCH_INTERVAL;
	}

	public void startGathering(EntityPlayer player) 
	{
		if (owner.posY <= 12)
		{
			owner.say("I can't create a mine here. We're too low!", player);
			return;
		}
		
		this.assigningPlayer = player.getPersistentID().toString();
		this.isGathering = true;
		this.isAIActive.setValue(true);
		this.activityInterval = 0;
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
}

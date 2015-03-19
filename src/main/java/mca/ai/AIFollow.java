package mca.ai;

import mca.core.Constants;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import radixcore.data.WatchedString;
import radixcore.util.RadixMath;

public class AIFollow extends AbstractAI
{
	private final WatchedString playerFollowingName;
	
	public AIFollow(EntityHuman entityHuman) 
	{
		super(entityHuman);
		playerFollowingName = new WatchedString("EMPTY", WatcherIDsHuman.PLAYER_FOLLOWING_NAME, owner.getDataWatcherEx());
	}

	@Override
	public void reset() 
	{	
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setString("playerFollowingName", playerFollowingName.getString());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		playerFollowingName.setValue(nbt.getString("playerFollowingName"));
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
		if (owner.getMovementState() == EnumMovementState.FOLLOW)
		{
			final EntityPlayer entityPlayer = owner.worldObj.getPlayerEntityByName(playerFollowingName.getString());
			
			if (entityPlayer != null)
			{
				final double distanceToPlayer = RadixMath.getDistanceToEntity(owner, entityPlayer);
				
				if (distanceToPlayer >= 10.0D)
				{
					final int playerX = net.minecraft.util.MathHelper.floor_double(entityPlayer.posX) - 2;
					final int playerY = net.minecraft.util.MathHelper.floor_double(entityPlayer.boundingBox.minY);
					final int playerZ = net.minecraft.util.MathHelper.floor_double(entityPlayer.posZ) - 2;

					for (int i = 0; i <= 4; ++i)
					{
						for (int i2 = 0; i2 <= 4; ++i2)
						{
							if ((i < 1 || i2 < 1 || i > 3 || i2 > 3) && World.doesBlockHaveSolidTopSurface(owner.worldObj, playerX + i, playerY - 1, playerZ + i2) && !owner.worldObj.getBlock(playerX + i, playerY, playerZ + i2).isNormalCube() && !owner.worldObj.getBlock(playerX + i, playerY + 1, playerZ + i2).isNormalCube())
							{
								owner.setLocationAndAngles(playerX + i + 0.5F, playerY, playerZ + i2 + 0.5F, entityPlayer.rotationYaw, entityPlayer.rotationPitch);
								owner.getNavigator().clearPathEntity();
							}
						}
					}
				}
				
				else if (distanceToPlayer >= 4.5D && owner.getNavigator().noPath())
				{
					owner.getNavigator().tryMoveToEntityLiving(entityPlayer, entityPlayer.isSprinting() ? Constants.SPEED_SPRINT : owner.getSpeed());
				}
				
				else if (distanceToPlayer <= 2.0D) //To avoid crowding the player.
				{
					owner.getNavigator().clearPathEntity();
				}
			}
			
			else
			{
				owner.setMovementState(EnumMovementState.MOVE);
			}
		}
	}
	
	public String getPlayerFollowingName()
	{
		return playerFollowingName.getString();
	}
	
	public void setPlayerFollowingName(String value)
	{
		playerFollowingName.setValue(value);
	}
}

package mca.ai;

import mca.core.Constants;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
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
			final EntityLiving entityPathController = (EntityLiving) (owner.getRidingEntity() instanceof EntityHorse ? owner.getRidingEntity() : owner);
			final EntityPlayer entityPlayer = owner.worldObj.getPlayerEntityByName(playerFollowingName.getString());

			if (entityPathController instanceof EntityHorse)
			{
				final EntityHorse horse = (EntityHorse) entityPathController;

				//This makes the horse move properly.
				if (horse.isHorseSaddled())
				{
					horse.setHorseSaddled(false);
				}
			}
			
			if (entityPlayer != null)
			{
				entityPathController.getLookHelper().setLookPositionWithEntity(entityPlayer, 10.0F, owner.getVerticalFaceSpeed());
				
				final double distanceToPlayer = RadixMath.getDistanceToEntity(owner, entityPlayer);

				//Crash was reported where bounding box ended up being null.
				if (distanceToPlayer >= 10.0D && entityPlayer.getEntityBoundingBox() != null)
				{
					final int i = net.minecraft.util.math.MathHelper.floor_double(entityPlayer.posX) - 2;
					final int j = net.minecraft.util.math.MathHelper.floor_double(entityPlayer.getEntityBoundingBox().minY);
					final int k = net.minecraft.util.math.MathHelper.floor_double(entityPlayer.posZ) - 2;

                    for (int l = 0; l <= 4; ++l)
                    {
                        for (int i1 = 0; i1 <= 4; ++i1)
                        {
                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && entityPathController.worldObj.getBlockState(new BlockPos(i + l, k - 1, j + i1)).isFullyOpaque() && this.isBlockSpawnable(new BlockPos(i + l, k, j + i1)) && this.isBlockSpawnable(new BlockPos(i + l, k + 1, j + i1)))
                            {
                                entityPathController.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), entityPathController.rotationYaw, entityPathController.rotationPitch);
                                entityPathController.getNavigator().clearPathEntity();
                                return;
                            }
                        }
                    }
				}

				else if (distanceToPlayer >= 4.5D && owner.getNavigator().noPath())
				{
					float speed = entityPathController instanceof EntityHorse ? Constants.SPEED_HORSE_RUN :  entityPlayer.isSprinting() ? Constants.SPEED_SPRINT : owner.getSpeed();
					entityPathController.getNavigator().tryMoveToEntityLiving(entityPlayer, speed);
				}

				else if (distanceToPlayer <= 2.0D) //To avoid crowding the player.
				{
					entityPathController.getNavigator().clearPathEntity();
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
	
    private boolean isBlockSpawnable(BlockPos pos)
    {
        IBlockState iblockstate = owner.worldObj.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block == Blocks.air ? true : !iblockstate.isFullCube();
    }
}

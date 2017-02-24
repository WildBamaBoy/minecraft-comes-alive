package mca.actions;

import java.util.UUID;

import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import radixcore.modules.RadixMath;

public class ActionFollow extends AbstractAction
{
	private UUID followingUUID;
	
	public ActionFollow(EntityVillagerMCA entityHuman) 
	{
		super(entityHuman);
		followingUUID = new UUID(0, 0);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setUniqueId("followingUUID", followingUUID);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		followingUUID = nbt.getUniqueId("followingUUID");
	}

	@Override
	public void onUpdateServer() 
	{
		if (actor.getMovementState() == EnumMovementState.FOLLOW)
		{
			final EntityLiving entityPathController = (EntityLiving) (actor.getRidingEntity() instanceof EntityHorse ? actor.getRidingEntity() : actor);
			final Entity entityFollowing = actor.world.getPlayerEntityByUUID(followingUUID);

			if (entityPathController instanceof EntityHorse)
			{
				final EntityHorse horse = (EntityHorse) entityPathController;

				//This makes the horse move properly.
				if (horse.isHorseSaddled())
				{
					horse.setHorseSaddled(false);
				}
			}

			if (entityFollowing != null)
			{
				entityPathController.getLookHelper().setLookPositionWithEntity(entityFollowing, 10.0F, actor.getVerticalFaceSpeed());

				final double distanceToTarget = RadixMath.getDistanceToEntity(actor, entityFollowing);

				//Crash was reported where bounding box ended up being null.
				if (distanceToTarget >= 15.0D && entityFollowing.getEntityBoundingBox() != null)
				{
					int i = MathHelper.floor(entityFollowing.posX) - 2;
					int j = MathHelper.floor(entityFollowing.posZ) - 2;
					int k = MathHelper.floor(entityFollowing.getEntityBoundingBox().minY);

					for (int l = 0; l <= 4; ++l)
					{
						for (int i1 = 0; i1 <= 4; ++i1)
						{
							if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && entityFollowing.world.getBlockState(new BlockPos(i + l, k - 1, j + i1)).isFullyOpaque() && this.isBlockSpawnable(new BlockPos(i + l, k, j + i1)) && isBlockSpawnable(new BlockPos(i + l, k + 1, j + i1)))
							{
								actor.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), actor.rotationYaw, actor.rotationPitch);
								actor.getNavigator().clearPathEntity();
								return;
							}
						}
					}
				}

				else if (distanceToTarget >= 4.5D && actor.getNavigator().noPath())
				{
					float speed = entityPathController instanceof EntityHorse ? Constants.SPEED_HORSE_RUN :  entityFollowing.isSprinting() ? Constants.SPEED_SPRINT : actor.getSpeed();
					entityPathController.getNavigator().tryMoveToEntityLiving(entityFollowing, speed);
					entityPathController.faceEntity(entityFollowing, 16.0F, 16.0F);
				}

				else if (distanceToTarget <= 2.0D) //To avoid crowding the player.
				{
					entityPathController.getNavigator().clearPathEntity();
				}
			}

			else
			{
				actor.setMovementState(EnumMovementState.MOVE);
			}
		}
	}

	public UUID getFollowingUUID()
	{
		return followingUUID;
	}

	public void setFollowingUUID(UUID value)
	{
		followingUUID = value;
	}

	private boolean isBlockSpawnable(BlockPos pos)
	{
		IBlockState iblockstate = actor.world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		return block == Blocks.AIR ? true : !iblockstate.isFullCube();
	}
}

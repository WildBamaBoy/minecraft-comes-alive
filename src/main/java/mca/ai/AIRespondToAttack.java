package mca.ai;

import mca.core.Constants;
import mca.entity.EntityHuman;
import mca.enums.EnumPersonality;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import radixcore.util.RadixMath;

public class AIRespondToAttack extends AbstractAI
{
	private String targetPlayerName;
	private Entity target;
	private boolean isRetaliating;

	public AIRespondToAttack(EntityHuman entityHuman) 
	{
		super(entityHuman);
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
		if (!owner.getIsChild() && isRetaliating)
		{
			if (target instanceof EntityPlayer)
			{
				final EntityPlayer targetPlayer = (EntityPlayer)target;

				if (targetPlayer != null)
				{
					double distanceToPlayer = RadixMath.getDistanceToEntity(owner, targetPlayer);

					if (distanceToPlayer >= 10.0D)
					{
						owner.say("behavior.retaliate.distanced", targetPlayer);
						reset();
					}

					else //Distance to player is within 10 blocks, we can continue chasing.
					{
						if (playerHasWeapon(targetPlayer)) //Stop chasing if the player draws a weapon.
						{
							handlePlayerWithWeapon();
						}

						else if (owner.getNavigator().noPath())
						{
							owner.getNavigator().tryMoveToEntityLiving(targetPlayer, Constants.SPEED_RUN);
						}

						else if (distanceToPlayer <= 1.8D)
						{
							targetPlayer.attackEntityFrom(DamageSource.generic, 1.0F);
							reset();
						}
					}
				}

				else //If target player is null for some reason, try to get it again and stop if it fails.
				{
					target = owner.worldObj.getPlayerEntityByName(targetPlayerName);

					if (target == null)
					{
						reset();
					}
				}
			}

			else if (target != null)
			{
				double distanceToTarget = RadixMath.getDistanceToEntity(owner, target);

				if (distanceToTarget >= 10.0D)
				{
					reset();
				}

				else
				{
					if (owner.getNavigator().noPath())
					{
						owner.getNavigator().tryMoveToEntityLiving(target, Constants.SPEED_RUN);
					}

					else if (distanceToTarget <= 1.8D)
					{
						target.attackEntityFrom(DamageSource.generic, 1.0F);
						reset();
					}
				}
			}
		}
	}

	@Override
	public void reset() 
	{
		isRetaliating = false;
		targetPlayerName = null;
		owner.getNavigator().clearPathEntity();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{

	}

	public void startResponse(Entity entity)
	{
		if (owner.getPersonality() == EnumPersonality.PEACEFUL)
		{
			return;
		}

		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entity;
			target = player;

			if (playerHasWeapon(player))
			{
				handlePlayerWithWeapon();
			}

			else
			{
				owner.say("behavior.retaliate.begin", player);

				isRetaliating = true;
				targetPlayerName = player.getCommandSenderName();
			}
		}

		else
		{
			target = entity;
			isRetaliating = true;
		}
	}

	private boolean playerHasWeapon(EntityPlayer player)
	{
		ItemStack heldItem = player.inventory.getCurrentItem();

		if (heldItem != null)
		{
			return heldItem.getItem() instanceof ItemSword || heldItem.getItem() instanceof ItemBow;
		}		

		return false;
	}

	private void handlePlayerWithWeapon()
	{
		try
		{
			owner.say("behavior.retaliate.weapondrawn", (EntityPlayer)target);
		}
		
		catch (NullPointerException e)
		{
			//NPE caused by anything that extends EntityPlayer and attacks the villager.
			//The PlayerMemory object cannot be created in this case since no player data will exist
			//for the "fake" player. This causes an NPE. Exception will simply be thrown away.
		}

		reset();
	}

	public boolean getIsRetaliating()
	{
		return isRetaliating;
	}
}

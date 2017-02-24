package mca.actions;

import mca.core.Constants;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfessionSkinGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;
import radixcore.modules.RadixMath;

public class ActionAttackResponse extends AbstractAction
{
	private String targetPlayerName;
	private Entity target;
	private boolean isRetaliating;

	public ActionAttackResponse(EntityVillagerMCA entityHuman) 
	{
		super(entityHuman);
	}

	@Override
	public void onUpdateServer() 
	{
		if (!actor.getIsChild() && isRetaliating && actor.getHealth() > 0.0F && !actor.getIsInfected())
		{
			if (target instanceof EntityPlayerMP && !target.getName().equals("[CoFH]") && !(target instanceof FakePlayer))
			{
				final EntityPlayer targetPlayer = (EntityPlayer)target;

				if (targetPlayer != null)
				{
					double distanceToPlayer = RadixMath.getDistanceToEntity(actor, targetPlayer);

					if (distanceToPlayer >= 10.0D)
					{
						actor.say("behavior.retaliate.distanced", targetPlayer);
						reset();
					}

					else //Distance to player is within 10 blocks, we can continue chasing.
					{
						if (playerHasWeapon(targetPlayer)) //Stop chasing if the player draws a weapon.
						{
							handlePlayerWithWeapon();
						}

						else if (actor.getNavigator().noPath())
						{
							actor.getNavigator().tryMoveToEntityLiving(targetPlayer, Constants.SPEED_RUN);
						}

						else if (distanceToPlayer <= 1.8D)
						{
							actor.swingItem();
							targetPlayer.attackEntityFrom(DamageSource.GENERIC, 1.0F);
							reset();
						}
					}
				}

				else //If target player is null for some reason, try to get it again and stop if it fails.
				{
					target = actor.world.getPlayerEntityByName(targetPlayerName);

					if (target == null)
					{
						reset();
					}
				}
			}

			else if (target != null)
			{
				double distanceToTarget = RadixMath.getDistanceToEntity(actor, target);

				if (distanceToTarget >= 10.0D)
				{
					reset();
				}

				else
				{
					if (actor.getNavigator().noPath())
					{
						actor.getNavigator().tryMoveToEntityLiving(target, Constants.SPEED_RUN);
					}

					else if (distanceToTarget <= 1.8D)
					{
						float attackDamage = actor.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard ? MCA.getConfig().guardAttackDamage : MCA.getConfig().villagerAttackDamage;
						actor.swingItem();
						target.attackEntityFrom(DamageSource.GENERIC, attackDamage);
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
		actor.getNavigator().clearPathEntity();
	}

	public void startResponse(Entity entity)
	{
		if (actor.getPersonality() == EnumPersonality.PEACEFUL)
		{
			return;
		}

		if (entity instanceof EntityPlayerMP && !entity.getName().equals("[CoFH]") && !(entity instanceof FakePlayer))
		{
			EntityPlayer player = (EntityPlayer)entity;
			target = player;

			if (playerHasWeapon(player))
			{
				handlePlayerWithWeapon();
			}

			else
			{
				actor.say("behavior.retaliate.begin", player);

				isRetaliating = true;
				targetPlayerName = player.getName();
				
				PlayerMemory memory = actor.getPlayerMemory(player);
				memory.setHearts(memory.getHearts() - 5);
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
			actor.say("behavior.retaliate.weapondrawn", (EntityPlayer)target);
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

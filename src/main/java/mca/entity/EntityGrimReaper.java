package mca.entity;

import java.util.List;

import mca.core.MCA;
import mca.core.minecraft.ModItems;
import mca.enums.EnumReaperAttackState;
import mca.packets.PacketSpawnLightning;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Particle;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class EntityGrimReaper extends EntityMob implements IBossDisplayData
{
	private int healingCooldown;
	private int timesHealed;
	private EntityLivingBase entityToAttack;
	
	@SideOnly(Side.CLIENT)
	private float floatingTicks;

	public EntityGrimReaper(World world) 
	{
		super(world);
		setSize(1.0F, 2.6F);
		this.experienceValue = 100;
	}

	@Override
	protected final void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(2.0F);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(12.5F);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(225.0F);
	}

	public IChatComponent func_145748_c_()
	{
		return new ChatComponentText("Grim Reaper");
	}

	@Override
	public boolean isAIDisabled() 
	{
		return true;
	}

	@Override
	protected void dropFewItems(boolean hitByPlayer, int lootingLvl) 
	{
		dropItem(ModItems.staffOfLife, 1);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(13, 0);
		this.dataWatcher.addObject(14, 0);
	}

	public void setAttackState(EnumReaperAttackState state)
	{
		//Only update if needed so that sounds only play once.
		if (this.dataWatcher.getWatchableObjectInt(13) != state.getId())
		{
			this.dataWatcher.updateObject(13, state.getId());

			switch (state)
			{
			case PRE: this.playSound("mca:reaper.scythe.out", 1.0F, 1.0F); break;
			case POST: this.playSound("mca:reaper.scythe.swing", 1.0F, 1.0F); break;
			}
		}
	}

	public EnumReaperAttackState getAttackState()
	{
		return EnumReaperAttackState.fromId(this.dataWatcher.getWatchableObjectInt(13));
	}

	public boolean hasEntitytoAttack()
	{
		return entityToAttack != null;
	}

	@Override
    public void onStruckByLightning(EntityLightningBolt entity)
    {
    	return;
    }
    
	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) 
	{	
		//Ignore wall damage and fire damage.
		if (source == DamageSource.inWall || source == DamageSource.onFire || source.isExplosion() || source == DamageSource.inFire)
		{
			//Teleport out of any walls we may end up in.
			if (source == DamageSource.inWall)
			{
				teleportTo(this.posX, this.posY + 3, this.posZ);
			}
			
			return false;
		}
		
		//Ignore damage when blocking, and teleport behind the player when they attempt to block.
		else if (!worldObj.isRemote && this.getAttackState() == EnumReaperAttackState.BLOCK && source.getSourceOfDamage() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) source.getSourceOfDamage();

			double deltaX = this.posX - player.posX;
			double deltaZ = this.posZ - player.posZ;

			this.playSound("mca:reaper.block", 1.0F, 1.0F);
			teleportTo(player.posX - (deltaX * 2), player.posY + 2, this.posZ - (deltaZ * 2));
			setStateTransitionCooldown(0);
			return false;
		}

		//Randomly portal behind the player who just attacked.
		else if (!worldObj.isRemote && source.getSourceOfDamage() instanceof EntityPlayer && RadixLogic.getBooleanWithProbability(30))
		{
			EntityPlayer player = (EntityPlayer) source.getSourceOfDamage();

			double deltaX = this.posX - player.posX;
			double deltaZ = this.posZ - player.posZ;

			teleportTo(player.posX - (deltaX * 2), player.posY + 2, this.posZ - (deltaZ * 2));
		}

		//Teleport behind the player who fired an arrow and ignore its damage.
		else if (source.getSourceOfDamage() instanceof EntityArrow)
		{
			EntityArrow arrow = (EntityArrow) source.getSourceOfDamage();

			if (arrow.shootingEntity instanceof EntityPlayer && getAttackState() != EnumReaperAttackState.REST)
			{
				EntityPlayer player = (EntityPlayer)arrow.shootingEntity;
				double newX = player.posX + (RadixLogic.getBooleanWithProbability(50) ? 2 : -2);
				double newZ = player.posZ + (RadixLogic.getBooleanWithProbability(50) ? 2 : -2);

				teleportTo(newX, player.posY, newZ);
			}

			arrow.setDead();
			return false;
		}

		//Still take damage when healing, but reduced by a third.
		else if (this.getAttackState() == EnumReaperAttackState.REST)
		{
			damage /= 3;
		}
		
		super.attackEntityFrom(source, damage);

		if (!worldObj.isRemote && this.getHealth() <= (this.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue() / 2) && healingCooldown == 0)
		{
			setAttackState(EnumReaperAttackState.REST);
			healingCooldown = (Time.MINUTE * 2) + (Time.SECOND * 30);
			teleportTo(this.posX, this.posY + 8, this.posZ);
			setStateTransitionCooldown(Time.MINUTE * 1);
		}

		return true;
	}

	protected void attackEntity(Entity entity, float damage) 
	{
		//Within 1.2 blocks of the target, damage it. Set attack state to post attack.
		if (RadixMath.getDistanceToEntity(entityToAttack, this) <= 1.8D)
		{
			entity.attackEntityFrom(DamageSource.causeMobDamage(this), 13.5F);
			
			if (entity instanceof EntityLivingBase)
			{
				((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.wither.id, this.worldObj.getDifficulty().getDifficultyId() * 20, 1));
			}
			
			setAttackState(EnumReaperAttackState.POST);
			setStateTransitionCooldown(10); //For preventing immediate return to the PRE or IDLE stage. Ticked down in onUpdate()
		}

		//Check if we're waiting for cooldown from the last attack.
		if (getStateTransitionCooldown() == 0)
		{
			//Within 3 blocks from the target, ready the scythe
			if (RadixMath.getDistanceToEntity(entityToAttack, this) <= 3.5D)
			{
				//Check to see if the player's blocking, then teleport behind them. 
				//Also randomly swap their selected item with something else in the hotbar and apply blindness.
				if (entityToAttack instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer)entityToAttack;

					if (player.isBlocking())
					{
						double dX = this.posX - player.posX;
						double dZ = this.posZ - player.posZ;

						teleportTo(player.posX - (dX * 2), player.posY + 2, this.posZ - (dZ * 2));

						if (!worldObj.isRemote && RadixLogic.getBooleanWithProbability(20))
						{
							int currentItem = player.inventory.currentItem;
							int randomItem = rand.nextInt(InventoryPlayer.getHotbarSize());
							ItemStack currentItemStack = player.inventory.mainInventory[currentItem];
							ItemStack randomItemStack = player.inventory.mainInventory[randomItem];

							player.inventory.mainInventory[currentItem] = randomItemStack;
							player.inventory.mainInventory[randomItem] = currentItemStack;
							
							player.addPotionEffect(new PotionEffect(Potion.blindness.id, this.worldObj.getDifficulty().getDifficultyId() * (Time.SECOND * 2), 1));
						}
					}

					else //If the player is not blocking, ready the scythe, or randomly block their attack.
					{
						//Don't block if we've already committed to an attack.
						if (RadixLogic.getBooleanWithProbability(40) && getAttackState() != EnumReaperAttackState.PRE)
						{
							setStateTransitionCooldown(Time.SECOND * 1);
							setAttackState(EnumReaperAttackState.BLOCK);
						}

						else
						{
							setAttackState(EnumReaperAttackState.PRE);
						}
					}
				}
			}

			else //Reset the attacking state when we're more than 3 blocks away.
			{
				setAttackState(EnumReaperAttackState.IDLE);
			}
		}
	}

	protected Entity findPlayerToAttack() 
	{
		return worldObj.getClosestPlayerToEntity(this, 48.0D);
	}

	@Override
	public int getTalkInterval() 
	{
		return Time.SECOND * 15;
	}

	@Override
	protected String getLivingSound()
	{
		return "mca:reaper.idle";
	}

	@Override
	protected String getDeathSound() 
	{
		return "mca:reaper.death";
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.wither.hurt";
	}

	@Override
	public void onUpdate() 
	{
		super.onUpdate();
		extinguish(); //No fire.
		
		//Increment floating ticks on the client when resting.
		if (worldObj.isRemote && getAttackState() == EnumReaperAttackState.REST)
		{
			floatingTicks += 0.1F;
			Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.SUSPENDED_DEPTH, this, 1);
			Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.CRIT_MAGIC, this, 1);
		}

		//Increase health when resting and check to stop rest state.
		//Runs on common to spawn lightning.
		if (getAttackState() == EnumReaperAttackState.REST)
		{
			if (!worldObj.isRemote && getStateTransitionCooldown() == 1)
			{
				setAttackState(EnumReaperAttackState.IDLE);
				timesHealed++;
			}

			else if (!worldObj.isRemote && getStateTransitionCooldown() % 100 == 0)
			{
				this.setHealth(this.getHealth() + MathHelper.clamp_float(10.5F - (timesHealed * 3.5F), 3.0F, 10.5F));

				//Let's have a light show.
				int dX = rand.nextInt(8) + 4 * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
				int dZ = rand.nextInt(8) + 4 * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
				int y = RadixLogic.getSpawnSafeTopLevel(worldObj, (int)posX + dX, (int)posZ + dZ);

				MCA.getPacketHandler().sendPacketToAllPlayers(new PacketSpawnLightning(new Point3D(posX + dX, y, posZ + dZ)));

				//Also spawn a random skeleton or zombie.
				if (!worldObj.isRemote)
				{
					EntityMob mob = RadixLogic.getBooleanWithProbability(50) ? new EntityZombie(worldObj) : new EntitySkeleton(worldObj);
					mob.setPosition(posX + dX + 4, y, posZ + dZ + 4);

					if (mob instanceof EntitySkeleton)
					{
						mob.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
					}

					worldObj.spawnEntityInWorld(mob);
				}
			}
		}

		//Prevent flying off into oblivion on death...
		if (this.getHealth() <= 0.0F)
		{
			motionX = 0;
			motionY = 0;
			motionZ = 0;
			return;
		}

		//Stop at our current position if resting
		if (getAttackState() == EnumReaperAttackState.REST)
		{
			motionX = 0;
			motionY = 0;
			motionZ = 0;
		}

		//Logic for flying.
		fallDistance = 0.0F;

		if (motionY > 0)
		{
			motionY = motionY * 1.04F;
		}

		else
		{
			double yMod = Math.sqrt((motionX * motionX) + (motionZ * motionZ));
			motionY = motionY * 0.6F + yMod * 0.3F;
		}

		//Tick down cooldowns.
		if (getStateTransitionCooldown() > 0)
		{
			setStateTransitionCooldown(getStateTransitionCooldown() - 1);
		}
		
		if (healingCooldown > 0)
		{
			healingCooldown--;
		}

		//See if our entity to attack has died at any point.
		if (entityToAttack != null && entityToAttack.isDead)
		{
			entityToAttack = null;
			setAttackState(EnumReaperAttackState.IDLE);
		}

		//Move towards target if we're not resting
		if (entityToAttack != null && getAttackState() != EnumReaperAttackState.REST)
		{
			//If we have a creature to attack, we need to move downwards if we're above it, and vice-versa.
			double sqDistanceTo = Math.sqrt(Math.pow(entityToAttack.posX - posX, 2) + Math.pow(entityToAttack.posZ - posZ, 2));
			float moveAmount = 0.0F;

			if(sqDistanceTo < 8F) 
			{ 
				moveAmount = ((8F - (float)sqDistanceTo) / 8F)*4F; 
			}

			if (entityToAttack.posY + 0.2F < posY)
			{
				motionY = motionY - 0.05F * moveAmount;
			}

			if (entityToAttack.posY - 0.5F > posY)
			{
				motionY = motionY + 0.01F * moveAmount;
			}

			//Speed up in order to lunge at the player.
			if (getAttackState() == EnumReaperAttackState.PRE)
			{
				motionX = motionX * 1.4F;
				motionZ = motionZ * 1.4F;
			}
		}

		//Kill plants close to us.
		if (!worldObj.isRemote)
		{
			List<Point3D> grassBlocks = RadixLogic.getNearbyBlocks(this, Blocks.grass, 1);

			for (Point3D point : grassBlocks)
			{
				BlockHelper.setBlock(worldObj, point, Blocks.dirt);
				Block blockAbove = BlockHelper.getBlock(worldObj, point.iPosX, point.iPosY + 1, point.iPosZ);

				if (blockAbove.getMaterial() == Material.plants || blockAbove.getMaterial() == Material.vine)
				{
					BlockHelper.setBlock(worldObj, point.iPosX, point.iPosY + 1, point.iPosZ, Blocks.air);
				}
			}
		}
	}

	@Override
	public void onDeath(DamageSource source) 
	{
		super.onDeath(source);

		for (int i = 0; i < 5; i++)
		{
			int dX = rand.nextInt(18) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
			int dZ = rand.nextInt(18) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
			EntityLightningBolt lightning = new EntityLightningBolt(worldObj, posX + dX, posY, posZ + dZ);
			worldObj.spawnEntityInWorld(lightning);
		}
	}

	@Override
	public String getName() 
	{
		return "Grim Reaper";
	}

	@Override
	protected boolean canDespawn() 
	{
		return true;
	}

	public void setStateTransitionCooldown(int value)
	{
		this.dataWatcher.updateObject(14, value);
	}

	public int getStateTransitionCooldown()
	{
		return this.dataWatcher.getWatchableObjectInt(14);
	}

	@SideOnly(Side.CLIENT)
	public float getFloatingTicks()
	{
		return floatingTicks;
	}

	private void teleportTo(double x, double y, double z)
	{
		if (!worldObj.isRemote)
		{
			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.PORTAL, this, 16);

			this.playSound("mob.endermen.portal", 2.0F, 1.0F);
			this.setPosition(x, y, z);
			this.playSound("mob.endermen.portal", 2.0F, 1.0F);

			Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.PORTAL, this, 16);
		}
	}
}

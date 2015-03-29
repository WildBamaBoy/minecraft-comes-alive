package mca.ai;

import java.util.Random;

import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMood;
import mca.enums.EnumPersonality;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Particle;
import radixcore.constant.Time;
import radixcore.data.WatchedFloat;
import radixcore.util.RadixMath;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AIMood extends AbstractAI
{
	private WatchedFloat moodValue;
	
	@SideOnly(Side.CLIENT)
	private int particleSpawnInterval;
	@SideOnly(Side.CLIENT)
	private int particleSpawnCounter;

	private int counter;
	
	public AIMood(EntityHuman entityHuman) 
	{
		super(entityHuman);

		moodValue = new WatchedFloat(5.0F, WatcherIDsHuman.MOOD_VALUE, entityHuman.getDataWatcherEx());
	}

	@Override
	public void onUpdateCommon() 
	{

	}

	@Override
	public void onUpdateClient() 
	{
		if (MCA.getConfig().showMoodParticles && getMoodLevel() != 0)
		{
			int moodLevel = getMoodLevel();
			String particles = "";

			switch (owner.getPersonality().getMoodGroup())
			{
			case GENERAL:
				particles = moodLevel > 0 ? Particle.HAPPY : Particle.SPLASH; break;
			case PLAYFUL:
				particles = moodLevel > 0 ? Particle.HAPPY : Particle.POTION_EFFECT; break;
			case SERIOUS:
				particles = moodLevel > 0 ? Particle.HAPPY : Particle.ANGRY; break;
			}
			
			switch (Math.abs(moodLevel))
			{
			case 1: particleSpawnInterval = 25; break;
			case 2: particleSpawnInterval = 15; break;
			case 3: particleSpawnInterval = 10; break;
			}

			if (particleSpawnCounter <= 0)
			{
				final Random rand = owner.worldObj.rand;
				final double velX = rand.nextGaussian() * 0.02D;
				final double velY = rand.nextGaussian() * 0.02D;
				final double velZ = rand.nextGaussian() * 0.02D;

				owner.worldObj.spawnParticle(particles, 
						owner.posX + rand.nextFloat() * owner.width * 2.0F - owner.width, 
						owner.posY + 0.5D + rand.nextFloat() * owner.height, 
						owner.posZ + rand.nextFloat() * owner.width * 2.0F - owner.width, 
						velX, velY, velZ);

				particleSpawnCounter = particleSpawnInterval;
			}

			else
			{
				particleSpawnCounter--;
			}
		}
	}

	@Override
	public void onUpdateServer() 
	{
		if (counter <= 0)
		{
			if (getMoodLevel() > 0)
			{
				modifyMoodLevel(-1.0F);
			}
			
			else if (getMoodLevel() < 0)
			{
				modifyMoodLevel(1.0F);
			}
			
			counter = Time.SECOND * 45;
		}
		
		counter--;
	}

	@Override
	public void reset() 
	{
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("moodValue", moodValue.getFloat());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		moodValue.setValue(nbt.getFloat("moodValue"));
	}

	public void modifyMoodLevel(float amount)
	{
		moodValue.setValue(RadixMath.clamp(moodValue.getFloat() + amount, 0.0F, 10.0F));
	}
	
	public EnumMood getMood(EnumPersonality personality)
	{
		return personality.getMoodGroup().getMood(getMoodLevel());
	}
	
	private int getMoodLevel()
	{
		int level = 0;
		
		switch (Math.round(moodValue.getFloat()))
		{
		case 0:  level = -3; break;
		case 1:  level = -2; break;
		case 2:  level = -2; break;
		case 3:  level = -1; break;
		case 4:  level = -1; break;
		case 5:  level = 0; break;
		case 6:  level = 1; break;
		case 7:  level = 1; break;
		case 8:  level = 2; break;
		case 9:  level = 2; break;
		case 10: level = 3; break;
		}

		return level;
	}
}

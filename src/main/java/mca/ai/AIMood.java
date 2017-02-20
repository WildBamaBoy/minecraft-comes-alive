package mca.ai;

import java.util.Random;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMood;
import mca.enums.EnumPersonality;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Time;
import radixcore.modules.RadixMath;

public class AIMood extends AbstractAI
{
	private final DataParameter<Float> MOOD_VALUE = EntityDataManager.<Float>createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
	
	@SideOnly(Side.CLIENT)
	private int particleSpawnInterval;
	@SideOnly(Side.CLIENT)
	private int particleSpawnCounter;

	private int counter;
	
	public AIMood(EntityVillagerMCA entityHuman) 
	{
		super(entityHuman);
		owner.getDataManager().set(MOOD_VALUE, 0.0F);
	}

	@Override
	public void onUpdateClient() 
	{
		if (MCA.getConfig().showMoodParticles && getMoodLevel() != 0)
		{
			int moodLevel = getMoodLevel();
			EnumParticleTypes particles = null;

			switch (owner.getPersonality().getMoodGroup())
			{
			case GENERAL:
				particles = moodLevel > 0 ? EnumParticleTypes.VILLAGER_HAPPY : EnumParticleTypes.WATER_SPLASH; break;
			case PLAYFUL:
				particles = moodLevel > 0 ? EnumParticleTypes.VILLAGER_HAPPY : EnumParticleTypes.SPELL_MOB; break;
			case SERIOUS:
				particles = moodLevel > 0 ? EnumParticleTypes.VILLAGER_HAPPY : EnumParticleTypes.VILLAGER_ANGRY; break;
			}
			
			switch (Math.abs(moodLevel))
			{
			case 1: particleSpawnInterval = 25; break;
			case 2: particleSpawnInterval = 15; break;
			case 3: particleSpawnInterval = 10; break;
			}

			if (particleSpawnCounter <= 0)
			{
				final Random rand = owner.world.rand;
				final double velX = rand.nextGaussian() * 0.02D;
				final double velY = rand.nextGaussian() * 0.02D;
				final double velZ = rand.nextGaussian() * 0.02D;

				owner.world.spawnParticle(particles, 
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
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("moodValue", getMoodValue());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setMoodValue(nbt.getFloat("moodValue"));
	}

	public void setMoodValue(float value)
	{
		owner.getDataManager().set(MOOD_VALUE, value);
	}
	
	public void modifyMoodLevel(float amount)
	{
		owner.getDataManager().set(MOOD_VALUE, RadixMath.clamp(getMoodValue() + amount, 0.0F, 10.0F));
	}
	
	public EnumMood getMood(EnumPersonality personality)
	{
		return personality.getMoodGroup().getMood(getMoodLevel());
	}
	
	private float getMoodValue()
	{
		return owner.getDataManager().get(MOOD_VALUE);
	}
	
	private int getMoodLevel()
	{
		int level = 0;
		
		switch (Math.round(getMoodValue()))
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

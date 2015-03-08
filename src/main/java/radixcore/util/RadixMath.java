package radixcore.util;

import java.util.Random;

import net.minecraft.entity.Entity;
import radixcore.math.Point3D;

public final class RadixMath
{
	private static final Random rand = new Random();
	
	public static boolean isWithinRange(int input, int minimum, int maximum)
	{
		return input >= minimum && input <= maximum;
	}

	public static boolean isWithinRange(float input, float minimum, float maximum)
	{
		return input >= minimum && input <= maximum;
	}
	
	public static int getNumberInRange(int minimum, int maximum)
	{
		return rand.nextInt(maximum - minimum + 1) + minimum;
	}

	public static float getNumberInRange(float minimum, float maximum)
	{
		return (rand.nextFloat() * (maximum - minimum)) + minimum;
	}
	
	public static double getDistanceToEntity(Entity entity1, Entity entity2)
	{
		return getDistanceToXYZ(entity1.posX, entity1.posY, entity1.posZ, entity2.posX, entity2.posY, entity2.posZ);
	}

	public static double getDistanceToXYZ(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		final double deltaX = x2 - x1;
		final double deltaY = y2 - y1;
		final double deltaZ = z2 - z1;

		return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	}
	
	public static double getDistanceToXYZ(Entity entity, Point3D point)
	{
		return getDistanceToXYZ(entity.posX, entity.posY, entity.posZ, point.dPosX, point.dPosY, point.dPosZ);
	}

	public static float getHighestNumber(float... numbers) 
	{
		float highestNumber = 0.0F;
		
		for (float i : numbers)
		{
			if (i > highestNumber)
			{
				highestNumber = i;
			}
		}
		
		return highestNumber;
	}
	
	public static int getHighestNumber(int... numbers)
	{
		int highestNumber = 0;
		
		for (int i : numbers)
		{
			if (i > highestNumber)
			{
				highestNumber = i;
			}
		}
		
		return highestNumber;
	}
	
	public static float getLowestNumber(float... numbers) 
	{
		float lowestNumber = 999.9F;
		
		for (float i : numbers)
		{
			if (i < lowestNumber)
			{
				lowestNumber = i;
			}
		}
		
		return lowestNumber;
	}
	
	public static int getLowestNumber(int... numbers)
	{
		int lowestNumber = 999;
		
		for (int i : numbers)
		{
			if (i < lowestNumber)
			{
				lowestNumber = i;
			}
		}
		
		return lowestNumber;
	}
	
	public static int clamp(int input, int min, int max)
	{
		return input < min ? min : input > max ? max : input;
	}
	
	public static float clamp(float input, float min, float max)
	{
		return input < min ? min : input > max ? max : input;
	}
	
	private RadixMath()
	{
	}
}

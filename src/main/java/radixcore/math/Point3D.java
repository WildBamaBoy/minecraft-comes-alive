/*******************************************************************************
 * Point3D.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package radixcore.math;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import radixcore.enums.EnumAxis;
import radixcore.util.RadixMath;

/**
 * Used to store a group of 3D coordinates and easily move them around.
 */
public final class Point3D implements Comparable
{
	public static final Point3D ZERO = new Point3D(0, 0, 0);
	
	public short sPosX;
	public short sPosY;
	public short sPosZ;

	public int iPosX;
	public int iPosY;
	public int iPosZ;

	public float fPosX;
	public float fPosY;
	public float fPosZ;

	public double dPosX;
	public double dPosY;
	public double dPosZ;

	public Point3D(short posX, short posY, short posZ)
	{
		sPosX = posX;
		sPosY = posY;
		sPosZ = posZ;

		iPosX = posX;
		iPosY = posY;
		iPosZ = posZ;

		fPosX = posX;
		fPosY = posY;
		fPosZ = posZ;

		dPosX = posX;
		dPosY = posY;
		dPosZ = posZ;
	}

	public Point3D(int posX, int posY, int posZ)
	{
		sPosX = (short) posX;
		sPosY = (short) posY;
		sPosZ = (short) posZ;

		iPosX = posX;
		iPosY = posY;
		iPosZ = posZ;

		fPosX = posX;
		fPosX = posY;
		fPosX = posZ;

		dPosX = posX;
		dPosY = posY;
		dPosZ = posZ;
	}

	public Point3D(float posX, float posY, float posZ)
	{
		sPosX = (short) posX;
		sPosY = (short) posY;
		sPosZ = (short) posZ;

		iPosX = (int) posX;
		iPosY = (int) posY;
		iPosZ = (int) posZ;

		fPosX = posX;
		fPosY = posY;
		fPosZ = posZ;

		dPosX = posX;
		dPosY = posY;
		dPosZ = posZ;
	}

	public Point3D(double posX, double posY, double posZ)
	{
		sPosX = (short) posX;
		sPosY = (short) posY;
		sPosZ = (short) posZ;

		iPosX = (int) posX;
		iPosY = (int) posY;
		iPosZ = (int) posZ;

		fPosX = (float) posX;
		fPosY = (float) posY;
		fPosZ = (float) posZ;

		dPosX = posX;
		dPosY = posY;
		dPosZ = posZ;
	}
	
	public Point3D setPoint(int posX, int posY, int posZ)
	{
		return new Point3D(posX, posY, posZ);
	}

	public Point3D setPoint(float posX, float posY, float posZ)
	{
		return new Point3D(posX, posY, posZ);
	}
	
	public Point3D setPoint(short posX, short posY, short posZ)
	{
		return new Point3D(posX, posY, posZ);
	}
	
	public Point3D setPoint(double posX, double posY, double posZ)
	{
		return new Point3D(posX, posY, posZ);
	}
	
	public static Point3D getNearestPointInList(Point3D refPoint, List<Point3D> pointList)
	{
		Point3D returnPoint = null;
		double lastDistance = 100.0D;
		
		for (Point3D point : pointList)
		{
			double distanceTo = RadixMath.getDistanceToXYZ(refPoint.iPosX, refPoint.iPosY, refPoint.iPosZ, point.iPosX, point.iPosY, point.iPosZ);
			
			if (distanceTo < lastDistance)
			{
				returnPoint = point;
				lastDistance = distanceTo;
			}
		}
		
		return returnPoint;
	}
	
	public Point3D rotate(EnumAxis axis, float angle)
	{
		if (axis == EnumAxis.X)
		{
			return new Point3D(
					dPosX, 
					dPosY * Math.cos(angle) - dPosZ * Math.sin(angle), 
					dPosY * Math.sin(angle) - dPosZ* Math.cos(angle));
		}
		
		else if (axis == EnumAxis.Y)
		{
			return new Point3D(
					dPosZ * Math.sin(angle) - dPosX * Math.cos(angle), 
					dPosY, 
					dPosZ * Math.cos(angle) - dPosX * Math.sin(angle));			
		}
		
		else if (axis == EnumAxis.Z)
		{
			return new Point3D(
					dPosX * Math.cos(angle) - dPosY * Math.sin(angle), 
					dPosX * Math.sin(angle) - dPosY * Math.cos(angle), 
					dPosZ);
		}
		
		else
		{
			return Point3D.ZERO;
		}
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt)
	{
		nbt.setDouble(name + "dPosX", dPosX);
		nbt.setDouble(name + "dPosY", dPosY);
		nbt.setDouble(name + "dPosZ", dPosZ);
	}
	
	public static Point3D readFromNBT(String name, NBTTagCompound nbt)
	{
		double x = nbt.getDouble(name + "dPosX");
		double y = nbt.getDouble(name + "dPosY");
		double z = nbt.getDouble(name + "dPosZ");
		
		if (x == 0 && y == 0 && z == 0)
		{
			return ZERO;
		}
		
		else
		{
			return new Point3D(x, y, z);
		}
	}
	
	/**
	 * Gets string representation of the Coordinates object.
	 * 
	 * @return "x, y, z" as string representation of the coordinates stored in this object.
	 */
	@Override
	public String toString()
	{
		return dPosX + ", " + dPosY + ", " + dPosZ;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Point3D)
		{
			final Point3D point = (Point3D)obj;
			return point.dPosX == this.dPosX && point.dPosY == this.dPosY && point.dPosZ == this.dPosZ;
		}
		
		return false;
	}

	@Override
	public int compareTo(Object obj) 
	{
		Point3D point = (Point3D)obj;
		
		if (this.iPosY > point.iPosY)
		{
			return 1;
		}
		
		else if (this.iPosY == point.iPosY)
		{
			if (this.iPosX > point.iPosX)
			{
				return 1;
			}
			
			else if (this.iPosX == point.iPosX)
			{
				if (this.iPosZ > point.iPosZ)
				{
					return 1;
				}
				
				else if (this.iPosZ == point.iPosZ)
				{
					return 0;
				}
				
				else
				{
					return -1;
				}
			}
			
			else
			{
				return -1;
			}
		}
		
		else
		{
			return -1;
		}
	}
}

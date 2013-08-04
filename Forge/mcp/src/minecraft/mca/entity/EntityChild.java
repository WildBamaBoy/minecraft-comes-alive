/*******************************************************************************
 * EntityChild.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import java.util.Calendar;

import mca.core.MCA;
import net.minecraft.world.World;

/**
 * Defines a child entity.
 */
public abstract class EntityChild extends AbstractEntity
{
	/** The age of the child in minutes. */
	public int age = 0;
	
	/** Is the child ready to grow up? */
	public boolean isReadyToGrow = false;
	
	/** Is the child fully grown? */
	public boolean isAdult = false;
	
	/** The name of the player who owns this child. */
	public String ownerPlayerName = "";

	/** The minute value gotten from the last minute value check. */
	protected int prevMinutes    = Calendar.getInstance().get(Calendar.MINUTE);
	
	/** The minute value gotten from the current minute value check. */
	protected int currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

	/**
	 * Constructor
	 * 
	 * @param 	world	An instance of the world object.
	 */
	public EntityChild(World world)
	{
		super(world);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		//Check if age should be increased.
		currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

		if (currentMinutes > prevMinutes || currentMinutes == 0 && prevMinutes == 59)
		{
			if (age < MCA.instance.modPropertiesManager.modProperties.kidGrowUpTimeMinutes)
			{
				age++;
				prevMinutes = currentMinutes;
			}
		}

		if (age >= MCA.instance.modPropertiesManager.modProperties.kidGrowUpTimeMinutes)
		{
			//Set age to the maximum to prevent the renderer from going nuts just in case age is higher than grow up time.
			age = MCA.instance.modPropertiesManager.modProperties.kidGrowUpTimeMinutes;
			isReadyToGrow = true;
		}
		
		//Debug checks
		//TODO: Enable
//		if (MCA.instance.inDebugMode)
//		{
//			age++;
//			
//			if (age >= MCA.instance.modPropertiesManager.modProperties.kidGrowUpTimeMinutes)
//			{
//				age = MCA.instance.modPropertiesManager.modProperties.kidGrowUpTimeMinutes;
//				isReadyToGrow = true;
//			}
//		}
	}
}

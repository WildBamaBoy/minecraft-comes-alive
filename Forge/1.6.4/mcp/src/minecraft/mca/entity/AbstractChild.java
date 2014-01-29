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
public abstract class AbstractChild extends AbstractEntity
{
	/** The age of the child in minutes. */
	public int age;

	public int currentGrowthMinutes = Calendar.getInstance().get(Calendar.MINUTE);
	public int prevGrowthMinutes = Calendar.getInstance().get(Calendar.MINUTE);
	
	/** Is the child ready to grow up? */
	public boolean isReadyToGrow;
	
	/** Is the child fully grown? */
	public boolean isAdult;
	
	/** The name of the player who owns this child. */
	public String ownerPlayerName = "";

	/**
	 * Constructor
	 * 
	 * @param 	world	An instance of the world object.
	 */
	public AbstractChild(World world)
	{
		super(world);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		//Check if age should be increased.
		currentGrowthMinutes = Calendar.getInstance().get(Calendar.MINUTE);

		if (MCA.getInstance().inDebugMode)
		{
			if (this instanceof EntityPlayerChild && !isAdult && MCA.getInstance().debugDoRapidPlayerChildGrowth)
			{
				age++;
			}
			
			else if (this instanceof EntityVillagerChild && MCA.getInstance().debugDoRapidVillagerChildGrowth)
			{
				age++;
			}
		}
		
		if (currentGrowthMinutes > prevGrowthMinutes || currentGrowthMinutes == 0 && prevGrowthMinutes == 59)
		{
			if (age < MCA.getInstance().modPropertiesManager.modProperties.kidGrowUpTimeMinutes)
			{
				age++;
				prevGrowthMinutes = currentGrowthMinutes;
			}
		}

		if (age >= MCA.getInstance().modPropertiesManager.modProperties.kidGrowUpTimeMinutes)
		{
			//Set age to the maximum to prevent the renderer from going nuts just in case age is higher than grow up time.
			age = MCA.getInstance().modPropertiesManager.modProperties.kidGrowUpTimeMinutes;
			isReadyToGrow = true;
		}
	}
}

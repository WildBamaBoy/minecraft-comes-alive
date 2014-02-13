/*******************************************************************************
 * AbstractVillagerPlugin.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.api;

import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.pathfinding.PathNavigate;

/**
 * An interface that plugs in to an MCA villager's methods to add or change their behavior.
 */
public abstract class AbstractVillagerPlugin 
{
	//TODO More things can be added here! Suggestions are welcome.
	
	/**
	 * Called after each call to addAI(). Modify and/or add AI tasks here.
	 * 
	 * @param 	villagerInfo	Standard information about this villager.
	 * @param 	tasks			The villager's AI tasks.
	 * @param 	navigator		The villager's path navigator.
	 */
	public abstract void onAddAI(VillagerInformation villagerInfo, EntityAITasks tasks, PathNavigate navigator);
}

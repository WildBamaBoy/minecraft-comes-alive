/*******************************************************************************
 * AbstractVillagerPlugin.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.villagers;

import net.minecraft.entity.EntityCreature;
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
	 * @param villager An instance of the villager, as an EntityCreature.
	 * @param villagerInfo Standard information about this villager.
	 * @param tasks The villager's AI tasks.
	 * @param navigator The villager's path navigator.
	 */
	public abstract void onAddAI(EntityCreature villager, VillagerInformation villagerInfo, EntityAITasks tasks, PathNavigate navigator);
}

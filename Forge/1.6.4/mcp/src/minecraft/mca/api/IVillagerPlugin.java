package mca.api;

import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.pathfinding.PathNavigate;

public interface IVillagerPlugin 
{
	//TODO More things can be added here! Suggestions are welcome.
	
	/**
	 * Called after each call to addAI(). Modify and/or add AI tasks here.
	 * 
	 * @param 	villagerInfo	Standard information about this villager.
	 * @param 	tasks			The villager's AI tasks.
	 * @param 	navigator		The villager's path navigator.
	 */
	public void addAI(VillagerInformation villagerInfo, EntityAITasks tasks, PathNavigate navigator);
}

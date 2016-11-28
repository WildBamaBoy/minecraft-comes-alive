/*******************************************************************************
 * EnumVillagerType.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.api.villagers;

/**
 * List of possible villager types.
 */
public enum EnumVillagerType 
{
	/** A regular, adult villager. */
	VillagerAdult,
	
	/** The child of two vilagers, not grown up yet. */
	VillagerChild,
	
	/** The child of a player, either grown or still a child. */
	PlayerChild;
}

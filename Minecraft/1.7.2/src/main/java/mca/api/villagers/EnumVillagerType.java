/*******************************************************************************
 * EnumVillagerType.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
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

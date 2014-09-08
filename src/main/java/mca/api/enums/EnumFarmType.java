/*******************************************************************************
 * EnumFarmType.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.enums;

/**
 * Defines the types of farms that can be created by MCA villagers.
 */
public enum EnumFarmType
{
	/**
	 * A "normal" farm is comprised of one or more 5 x 5 squares with one water block in the middle. In MCA, "normal" farms are created with wheat, potatoes, and carrots.
	 */
	NORMAL,

	/**
	 * A block farm is intended for crops that act similarly to melons and pumpkins. It allows for a stem to grow and leaves room for its crop to be placed.
	 */
	BLOCK,

	/**
	 * A sugarcane farm is intended for crops that require water to be beside them. If your crop behaves like sugarcane, use the sugarcane farm. <b>Farmland will NOT be placed.</b> Grass is placed instead.
	 */
	SUGARCANE;
}

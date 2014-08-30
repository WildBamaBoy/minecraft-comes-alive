/*******************************************************************************
 * EnumCrownColor.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.enums;

/**
 * Defines the available colors for the decorative crowns.
 */
public enum EnumCrownColor
{
	Red("Red"), Green("Green"), Blue("Blue"), Pink("Pink"), Purple("Purple");

	private String colorName;

	private EnumCrownColor(String name)
	{
		colorName = name;
	}

	public String getColorName()
	{
		return colorName;
	}
}

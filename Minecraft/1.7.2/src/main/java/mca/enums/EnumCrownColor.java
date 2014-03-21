/*******************************************************************************
 * EnumCrownColor.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.enums;

/**
 * Defines the available colors for the decorative crowns.
 */
public enum EnumCrownColor 
{
	Red("Red"),
	Green("Green"),
	Blue("Blue"),
	Pink("Pink"),
	Purple("Purple");
	
	private String colorName;
	
	private EnumCrownColor(String name)
	{
		this.colorName = name;
	}
	
	public String getColorName()
	{
		return colorName;
	}
}

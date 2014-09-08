/*******************************************************************************
 * TypeIDs.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.network.packets;

public final class TypeIDs
{
	private TypeIDs()
	{		
	}
	
	public final class Procreation
	{
		public static final int START = 0;
		public static final int STOP = 1;
		public static final int START_CLIENT = 2;
	};
}

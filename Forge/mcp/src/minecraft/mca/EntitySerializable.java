/*******************************************************************************
 * EntitySerializable.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;

/**
 * Allows serialization of an entity.
 */
public abstract class EntitySerializable extends EntityVillager
{
	/**
	 * Constructor
	 */
	public EntitySerializable()
	{
		super(null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param 	world	The world that the entity should spawn in.
	 */
	public EntitySerializable(World world)
	{
		super(world);
	}
}

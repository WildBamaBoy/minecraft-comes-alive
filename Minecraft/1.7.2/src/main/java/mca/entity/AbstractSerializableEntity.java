/*******************************************************************************
 * AbstractSerializableEntity.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.entity;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;

/**
 * Allows serialization of an entity.
 */
public abstract class AbstractSerializableEntity extends EntityVillager
{
	/**
	 * Constructor
	 */
	public AbstractSerializableEntity()
	{
		super(null);
	}

	/**
	 * Constructor
	 * 
	 * @param world The world that the entity should spawn in.
	 */
	public AbstractSerializableEntity(World world)
	{
		super(world);
	}
}

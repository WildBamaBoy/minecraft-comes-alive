/*******************************************************************************
 * HuntableAnimal.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.chores;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * An animal that can be hunted in the hunting chore.
 */
public class HuntableAnimal
{
	private final Class animalClass;
	private final Item killingItem;
	private final Block killingBlock;
	private final Item tamingItem;
	private final int probabilityOfSuccess;
	private final boolean isKillable;
	private final boolean isTameable;

	/**
	 * Constructs a new huntable animal that <b>returns an <u>item</u> when killed.</b>
	 * <p>
	 * Note that you can control whether or not the animal can be killed and/or tamed. Shown in the examples below.
	 * <p>
	 * <b>Ex. a typical tameable and killable animal:</b> <code>
	 * new HuntableAnimal(EntityPig.class, Items.porkchop, Items.carrot, 70, true, true); <p>
	 * </code> <b>Ex: an unkillable animal that can only be tamed.</b> <code>
	 * new HuntableAnimal(EntityWolf.class, (Item)null, Items.bone, 33, false, true); <p>
	 * </code>
	 * 
	 * @param animalClass The animal's class. The class must contain a constructor that accepts only a World as an argument.
	 * @param killingBlock The block added to the hunter's inventory upon a successful killing.
	 * @param tamingItem The item used to tame the animal. It is consumed from the hunter's inventory upon a successful taming.
	 * @param probabilityOfSuccess The percentage probability that the animal will be tamed.
	 * @param isKillable Is this animal killable via the "Kill" method of the hunting chore?
	 * @param isTameable Is this animal tameable via the "Tame" method of the hunting chore?
	 */
	public HuntableAnimal(Class animalClass, Item killingItem, Item tamingItem, int probabilityOfSuccess, boolean isKillable, boolean isTameable)
	{
		this.animalClass = animalClass;
		this.killingItem = killingItem;
		killingBlock = null;
		this.tamingItem = tamingItem;
		this.probabilityOfSuccess = probabilityOfSuccess;
		this.isKillable = isKillable;
		this.isTameable = isTameable;
	}

	/**
	 * Constructs a new huntable animal that <b>returns a <u>block</u> when killed.</b> This is used for sheep in MCA.
	 * <p>
	 * <b>Ex:</b> <code>
	 * new HuntableAnimal(EntitySheep.class, Blocks.wool, Items.wheat, 50, true, true);
	 * </code>
	 * 
	 * @param animalClass The animal's class. The class must contain a constructor that accepts only a World as an argument.
	 * @param killingBlock The block added to the hunter's inventory upon a successful killing.
	 * @param tamingItem The item used to tame the animal. It is consumed from the hunter's inventory upon a successful taming.
	 * @param probabilityOfSuccess The percentage probability that the animal will be tamed.
	 * @param isKillable Is this animal killable via the "Kill" method of the hunting chore?
	 * @param isTameable Is this animal tameable via the "Tame" method of the hunting chore?
	 */
	public HuntableAnimal(Class animalClass, Block killingBlock, Item tamingItem, int probabilityOfSuccess, boolean isKillable, boolean isTameable)
	{
		this.animalClass = animalClass;
		killingItem = null;
		this.killingBlock = killingBlock;
		this.tamingItem = tamingItem;
		this.probabilityOfSuccess = probabilityOfSuccess;
		this.isKillable = isKillable;
		this.isTameable = isTameable;
	}

	public Class getAnimalClass()
	{
		return animalClass;
	}

	public Item getKillingItem()
	{
		return killingItem;
	}

	public Block getKillingBlock()
	{
		return killingBlock;
	}

	public boolean isBlock()
	{
		return killingBlock != null;
	}

	public Item getTamingItem()
	{
		return tamingItem;
	}

	public int getProbabilityOfSuccess()
	{
		return probabilityOfSuccess;
	}

	public boolean getIsKillable()
	{
		return isKillable;
	}

	public boolean getIsTameable()
	{
		return isTameable;
	}
}

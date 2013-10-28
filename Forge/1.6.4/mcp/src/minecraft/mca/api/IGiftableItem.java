/*******************************************************************************
 * IGiftableItem.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.api;


/**
 * Allows an item to be gifted to an MCA villager.
 */
public interface IGiftableItem 
{
	/**
	 * Provides gift value of the item. Note that MCA villagers will automatically accept and place items that
	 * extend from ItemTool, ItemSword, or ItemArmor into their inventory. If the villager is a player's child,
	 * they will also equip items that extend from ItemHoe or ItemFishingRod.
	 * 
	 * If you implement this interface on an item that extends from any of the mentioned classes, the item will 
	 * be treated as a regular gift and disappear permanently while increasing a villagers' hearts.
	 * 
	 * @return	Base amount that hearts will increase when item is gifted. This is modified by a villager's 
	 * 			interaction fatigue, mood, and trait.
	 */
	public int getGiftValue();
}
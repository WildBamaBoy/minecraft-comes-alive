/*******************************************************************************
 * IGiftableItem.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.items;

import mca.api.villagers.VillagerInformation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Allows an item to be gifted to an MCA villager.
 */
public interface IGiftableItem
{
	/**
	 * Provides gift value of the item. Note that MCA villagers will automatically accept and place items that extend from ItemTool, ItemSword, or ItemArmor into their inventory. If the villager is a player's child, they will also equip items that extend from ItemHoe or ItemFishingRod. If you implement this interface on an item that extends from any of the mentioned classes, you will override this behavior and may cause some confusion.
	 * 
	 * @return The base amount that hearts will increase when item is gifted. This is modified by a villager's interaction fatigue, mood, and trait. You cannot disable these modifications.
	 */
	int getGiftValue();

	/**
	 * This method will be called before a villager accepts this gift. Use this, for example, to check and see if certain conditions are met before the gift is actually consumed.
	 * 
	 * @param villagerInfo Information pertaining to the villager receiving the gift.
	 * @param player The player that gifted the villager.
	 * @param giftStack The ItemStack that was gifted to the villager.
	 * @param posX The villager's posX.
	 * @param posY The villager's posY.
	 * @param posZ The villager's posZ.
	 * @return True if the gift is valid and can be consumed. False if otherwise. Note that if false is returned, a villager will not say anything to indicate that gifting failed. You must do this yourself.
	 */
	boolean doPreCallback(VillagerInformation villagerInfo, EntityPlayer player, ItemStack giftStack, double posX, double posY, double posZ);

	/**
	 * This method will be called after a villager accepts this gift. Use this, for example, to register certain conditions or make something happen after the gift is accepted and consumed.
	 * 
	 * @param villagerInfo Information pertaining to the villager receiving the gift.
	 * @param player The player that gifted the villager.
	 * @param giftStack The ItemStack that was gifted to the villager.
	 * @param posX The villager's posX.
	 * @param posY The villager's posY.
	 * @param posZ The villager's posZ.
	 */
	void doPostCallback(VillagerInformation villagerInfo, EntityPlayer player, ItemStack giftStack, double posX, double posY, double posZ);
}

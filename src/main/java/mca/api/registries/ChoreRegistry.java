/*******************************************************************************
 * ChoreRegistry.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.api.registries;

import java.util.ArrayList;
import java.util.List;

import mca.api.chores.CatchableFish;
import mca.api.chores.CookableFood;
import mca.api.chores.CuttableLog;
import mca.api.chores.FarmableCrop;
import mca.api.chores.FishingReward;
import mca.api.chores.HuntableAnimal;
import mca.api.chores.MineableOre;

public final class ChoreRegistry
{
	private static List<CookableFood> cookingEntries = new ArrayList<CookableFood>();
	private static List<FarmableCrop> farmingCropEntries = new ArrayList<FarmableCrop>();
	private static List<FishingReward> fishingFindEntries = new ArrayList<FishingReward>();
	private static List<CatchableFish> fishingFishEntries = new ArrayList<CatchableFish>();
	private static List<HuntableAnimal> huntingAnimalEntries = new ArrayList<HuntableAnimal>();
	private static List<MineableOre> miningOreEntries = new ArrayList<MineableOre>();
	private static List<CuttableLog> woodcuttingTreeEntries = new ArrayList<CuttableLog>();

	public static void registerChoreEntry(Object entry)
	{
		if (entry instanceof CookableFood)
		{
			cookingEntries.add((CookableFood) entry);
		}

		else if (entry instanceof FarmableCrop)
		{
			farmingCropEntries.add((FarmableCrop) entry);
		}

		else if (entry instanceof FishingReward)
		{
			fishingFindEntries.add((FishingReward) entry);
		}

		else if (entry instanceof CatchableFish)
		{
			fishingFishEntries.add((CatchableFish) entry);
		}

		else if (entry instanceof HuntableAnimal)
		{
			huntingAnimalEntries.add((HuntableAnimal) entry);
		}

		else if (entry instanceof MineableOre)
		{
			miningOreEntries.add((MineableOre) entry);
		}

		else if (entry instanceof CuttableLog)
		{
			woodcuttingTreeEntries.add((CuttableLog) entry);
		}

		else
		{
			throw new IllegalArgumentException("Chore entry object provided does not match any valid entry type.");
		}
	}

	public static List<CookableFood> getCookingEntries()
	{
		return cookingEntries;
	}

	public static List<FarmableCrop> getFarmingCropEntries()
	{
		return farmingCropEntries;
	}

	public static List<FishingReward> getFishingFindEntries()
	{
		return fishingFindEntries;
	}

	public static List<CatchableFish> getFishingFishEntries()
	{
		return fishingFishEntries;
	}

	public static List<HuntableAnimal> getHuntingAnimalEntries()
	{
		return huntingAnimalEntries;
	}

	public static List<MineableOre> getMiningOreEntries()
	{
		return miningOreEntries;
	}

	public static List<CuttableLog> getWoodcuttingTreeEntries()
	{
		return woodcuttingTreeEntries;
	}
}

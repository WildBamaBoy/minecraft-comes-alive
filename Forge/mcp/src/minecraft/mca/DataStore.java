/*******************************************************************************
 * DataStore.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Holds various lists and maps of data.
 */
public final class DataStore 
{
	/** List of the male names loaded from MaleNames.txt.*/
	public static List<String> maleNames = new ArrayList<String>();

	/** List of the female names loaded from FemaleNames.txt.*/
	public static List<String> femaleNames = new ArrayList<String>();

	/** List of the locations of male farmer skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> farmerSkinsMale = new ArrayList<String>();

	/** List of the locations of male librarian skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> librarianSkinsMale = new ArrayList<String>();

	/** List of the locations of male priest skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> priestSkinsMale = new ArrayList<String>();

	/** List of the locations of male smith skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> smithSkinsMale  = new ArrayList<String>();

	/** List of the locations of male butcher skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> butcherSkinsMale = new ArrayList<String>();

	/** List of the locations of male guard skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> guardSkinsMale = new ArrayList<String>();

	/** List of the locations of male kid skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> kidSkinsMale = new ArrayList<String>();

	/** List of the locations of male baker skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> bakerSkinsMale = new ArrayList<String>();

	/** List of the locations of male miner skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> minerSkinsMale = new ArrayList<String>();

	/** List of the locations of female farmer skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> farmerSkinsFemale = new ArrayList<String>();

	/** List of the locations of female librarian skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> librarianSkinsFemale = new ArrayList<String>();

	/** List of the locations of female priest skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> priestSkinsFemale = new ArrayList<String>();

	/** List of the locations of female smith skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> smithSkinsFemale = new ArrayList<String>();

	/** List of the locations of female butcher skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> butcherSkinsFemale = new ArrayList<String>();

	/** List of the locations of female guard skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> guardSkinsFemale = new ArrayList<String>();

	/** List of the locations of female kid skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> kidSkinsFemale = new ArrayList<String>();

	/** List of the locations of female baker skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> bakerSkinsFemale	= new ArrayList<String>();

	/** List of the locations of female miner skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> minerSkinsFemale	= new ArrayList<String>();

	/** Map of the IDs of items and the amount of hearts given to the villager who receives this item.*/
	public static Map<Integer, Integer> acceptableGifts = new HashMap<Integer, Integer>();

	/** 2D array containing the item IDs of wedding gifts considered junk gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingJunkGiftIDs = 
		{
		{Block.dirt.blockID, 1, 6},
		{Block.deadBush.blockID, 1, 1},
		{Block.cactus.blockID, 1, 3},
		{Item.stick.itemID, 1, 4},
		{Item.rottenFlesh.itemID, 1, 4},
		};

	/** 2D array containing the item IDs of wedding gifts considered small gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingSmallGiftIDs =
		{
		{Item.clay.itemID, 4, 16},
		{Item.axeStone.itemID, 1, 1},
		{Item.swordStone.itemID, 1, 1},
		{Item.shovelStone.itemID, 1, 1},
		{Item.appleRed.itemID, 1, 4},
		{Item.arrow.itemID, 8, 16},
		{Item.pickaxeStone.itemID, 1, 1},
		{Item.book.itemID, 1, 2},
		{Item.redstone.itemID, 8, 32},
		{Item.porkCooked.itemID, 3, 6},
		{Item.beefCooked.itemID, 3, 6},
		{Item.chickenCooked.itemID, 3, 6},
		{Item.bread.itemID, 1, 3},
		{Block.planks.blockID, 2, 16},
		{Block.wood.blockID, 2, 16},
		{Block.cobblestone.blockID, 2, 16},
		{Item.coal.itemID, 2, 8}
		};

	/** 2D array containing the item IDs of wedding gifts considered regular gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingRegularGiftIDs =
		{
		{Item.clay.itemID, 16, 32},
		{Item.axeIron.itemID, 1, 1},
		{Item.swordIron.itemID, 1, 1},
		{Item.shovelIron.itemID, 1, 1},
		{Item.arrow.itemID, 16, 32},
		{Item.pickaxeIron.itemID, 1, 1},
		{Item.redstone.itemID, 8, 32},
		{Item.porkCooked.itemID, 6, 8},
		{Item.beefCooked.itemID, 6, 8},
		{Item.chickenCooked.itemID, 6, 8},
		{Block.planks.blockID, 16, 32},
		{Block.wood.blockID, 16, 32},
		{Block.cobblestone.blockID, 16, 32},
		{Item.coal.itemID, 10, 16},
		{Item.legsIron.itemID, 1, 1},
		{Item.helmetIron.itemID, 1, 1},
		{Item.bootsIron.itemID, 1, 1},
		{Item.plateIron.itemID, 1, 1},
		{Item.melon.itemID, 4, 8},
		{Block.bookShelf.blockID, 2, 4},
		{Item.ingotIron.itemID, 8, 16}
		};

	/** 2D array containing the item IDs of wedding gifts considered great gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingGreatGiftIDs =
		{
		{Block.brick.blockID, 32, 32},
		{Item.axeDiamond.itemID, 1, 1},
		{Item.swordDiamond.itemID, 1, 1},
		{Item.shovelDiamond.itemID, 1, 1},
		{Item.arrow.itemID, 64, 64},
		{Item.pickaxeDiamond.itemID, 1, 1},
		{Block.planks.blockID, 32, 64},
		{Block.wood.blockID, 32, 64},
		{Block.cobblestone.blockID, 32, 64},
		{Item.coal.itemID, 32, 64},
		{Item.legsDiamond.itemID, 1, 1},
		{Item.helmetDiamond.itemID, 1, 1},
		{Item.bootsDiamond.itemID, 1, 1},
		{Item.plateDiamond.itemID, 1, 1},
		{Item.eyeOfEnder.itemID, 4, 8},
		{Block.enchantmentTable.blockID, 1, 1},
		{Block.cobblestoneMossy.blockID, 32, 64},
		{Item.diamond.itemID, 8, 16},
		{Block.jukebox.blockID, 1, 1},
		{Block.blockDiamond.blockID, 1, 2},
		{Block.blockGold.blockID, 1, 4},
		{Block.blockIron.blockID, 1, 8},
		{Block.obsidian.blockID, 4, 8},
		{Item.emerald.itemID, 4, 6}
		};

	/** 2D array containing the item IDs of items that a farmer may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] farmerAidIDs =
		{
		{Item.wheat.itemID, 1, 4},
		{Item.appleRed.itemID, 1, 3},
		{Item.seeds.itemID, 3, 12},
		{Item.reed.itemID, 3, 6},
		{Item.carrot.itemID, 3, 6},
		{Item.potato.itemID, 2, 4},
		};

	/** 2D array containing the item IDs of items that a butcher may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] butcherAidIDs =
		{
		{Item.beefRaw.itemID, 1, 3},
		{Item.porkRaw.itemID, 1, 3},
		{Item.chickenRaw.itemID, 1, 3},
		{Item.leather.itemID, 2, 6},
		{Item.feather.itemID, 6, 12},
		};

	/** 2D array containing the item IDs of items that a baker may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] bakerAidIDs =
		{
		{Item.bread.itemID, 1, 4},
		{Item.cake.itemID, 1, 1},
		};

	/**
	 * Map that contains string translations loaded from language files.
	 * Key will be the ID of the phrase.
	 * Value will be the translated representation of the phrase.
	 */
	public static Map<String, String> stringTranslations = new HashMap();

	/**
	 * Writes the contents of every list in this class to the console for debug purposes.
	 */
	public static void displayAllData()
	{
		for (Field f : DataStore.class.getFields())
		{
			if (f.getType().equals(List.class))
			{
				try
				{
					List theList = (List)f.get(null);

					for (Object obj : theList)
					{
						MCA.instance.log(obj.toString());
					}
				}

				catch (Throwable e)
				{
					MCA.instance.log("Error displaying all data within data store.");
				}
			}
		}
	}

	/**
	 * Gets the appropriate skin list for the entity provided.
	 * 
	 * @param 	entity	The entity that needs a list of valid skins.
	 * 
	 * @return	A list of skins that are valid for the provided entity.
	 */
	public static List<String> getSkinList(EntityBase entity)
	{
		if (!(entity instanceof EntityPlayerChild))
		{
			if (entity.gender.equals("Male"))
			{
				switch (entity.profession)
				{
				case 0: return DataStore.farmerSkinsMale;
				case 1: return DataStore.librarianSkinsMale;
				case 2: return DataStore.priestSkinsMale;
				case 3: return DataStore.smithSkinsMale;
				case 4: return DataStore.butcherSkinsMale;
				case 5: return DataStore.guardSkinsMale;
				case 6: return DataStore.bakerSkinsMale;
				case 7: return DataStore.minerSkinsMale;
				}
			}

			else
			{
				switch (entity.profession)
				{
				case 0: return DataStore.farmerSkinsFemale;
				case 1: return DataStore.librarianSkinsFemale;
				case 2: return DataStore.priestSkinsFemale;
				case 3: return DataStore.smithSkinsFemale;
				case 4: return null;
				case 5: return DataStore.guardSkinsFemale;
				case 6: return DataStore.bakerSkinsFemale;
				case 7: return DataStore.minerSkinsFemale;
				}
			}
		}
		
		else
		{
			if (entity.gender.equals("Male"))
			{
				return DataStore.kidSkinsMale;
			}
			
			else if (entity.gender.equals("Female"))
			{
				return DataStore.kidSkinsFemale;
			}
		}
		
		return null;
	}

	static
	{
		acceptableGifts.put(Item.swordWood.itemID, 3);
		acceptableGifts.put(Item.axeWood.itemID, 3);
		acceptableGifts.put(Item.hoeWood.itemID, 3);
		acceptableGifts.put(Item.shovelWood.itemID, 3);
		acceptableGifts.put(Item.swordStone.itemID, 5);
		acceptableGifts.put(Item.axeStone.itemID, 5);
		acceptableGifts.put(Item.hoeStone.itemID, 5);
		acceptableGifts.put(Item.shovelStone.itemID, 5);
		acceptableGifts.put(Item.pickaxeWood.itemID, 3);
		acceptableGifts.put(Item.beefRaw.itemID, 2);
		acceptableGifts.put(Item.chickenRaw.itemID, 2);
		acceptableGifts.put(Item.porkRaw.itemID, 2);
		acceptableGifts.put(Item.leather.itemID, 2);
		acceptableGifts.put(Item.plateLeather.itemID, 5);
		acceptableGifts.put(Item.helmetLeather.itemID, 5);
		acceptableGifts.put(Item.legsLeather.itemID, 5);
		acceptableGifts.put(Item.bootsLeather.itemID, 5);
		acceptableGifts.put(Item.reed.itemID, 2);
		acceptableGifts.put(Item.seeds.itemID, 2);
		acceptableGifts.put(Item.wheat.itemID, 3);
		acceptableGifts.put(Item.bread.itemID, 6);
		acceptableGifts.put(Item.coal.itemID, 5);
		acceptableGifts.put(Item.sugar.itemID, 5);
		acceptableGifts.put(Item.clay.itemID, 2);
		acceptableGifts.put(Item.dyePowder.itemID, 1);

		acceptableGifts.put(Item.beefCooked.itemID, 7);
		acceptableGifts.put(Item.chickenCooked.itemID, 7);
		acceptableGifts.put(Item.porkCooked.itemID, 7);
		acceptableGifts.put(Item.cookie.itemID, 10);
		acceptableGifts.put(Item.melon.itemID, 10);
		acceptableGifts.put(Item.melonSeeds.itemID, 5);
		acceptableGifts.put(Item.helmetIron.itemID, 10);
		acceptableGifts.put(Item.plateIron.itemID, 10);
		acceptableGifts.put(Item.legsIron.itemID, 10);
		acceptableGifts.put(Item.bootsIron.itemID, 10);
		acceptableGifts.put(Item.cake.itemID, 12);
		acceptableGifts.put(Item.swordIron.itemID, 10);
		acceptableGifts.put(Item.axeIron.itemID, 10);
		acceptableGifts.put(Item.hoeIron.itemID, 10);
		acceptableGifts.put(Item.pickaxeIron.itemID, 10);
		acceptableGifts.put(Item.shovelIron.itemID, 10);
		acceptableGifts.put(Item.fishingRod.itemID, 3);
		acceptableGifts.put(Item.bow.itemID, 5);
		acceptableGifts.put(Item.book.itemID, 5);
		acceptableGifts.put(Item.bucketEmpty.itemID, 3);
		acceptableGifts.put(Item.bucketMilk.itemID, 5);
		acceptableGifts.put(Item.bucketWater.itemID, 2);
		acceptableGifts.put(Item.bucketLava.itemID, 2);
		acceptableGifts.put(Item.bowlSoup.itemID, 5);
		acceptableGifts.put(Item.pumpkinSeeds.itemID, 8);
		acceptableGifts.put(Item.flintAndSteel.itemID, 4);
		acceptableGifts.put(Item.redstone.itemID, 5);
		acceptableGifts.put(Item.boat.itemID, 4);
		acceptableGifts.put(Item.doorWood.itemID, 4);
		acceptableGifts.put(Item.doorIron.itemID, 6);
		acceptableGifts.put(Item.minecartEmpty.itemID, 3);
		acceptableGifts.put(Item.minecartCrate.itemID, 5);
		acceptableGifts.put(Item.minecartPowered.itemID, 7);
		acceptableGifts.put(Item.flint.itemID, 2);
		acceptableGifts.put(Item.goldNugget.itemID, 4);
		acceptableGifts.put(Item.ingotGold.itemID, 20);
		acceptableGifts.put(Item.ingotIron.itemID, 10);

		acceptableGifts.put(Item.diamond.itemID, 30);
		acceptableGifts.put(Item.map.itemID, 10);
		acceptableGifts.put(Item.pocketSundial.itemID, 5);
		acceptableGifts.put(Item.compass.itemID, 5);
		acceptableGifts.put(Item.blazeRod.itemID, 10);
		acceptableGifts.put(Item.blazePowder.itemID, 5);
		acceptableGifts.put(Item.swordDiamond.itemID, 15);
		acceptableGifts.put(Item.axeDiamond.itemID, 15);
		acceptableGifts.put(Item.shovelDiamond.itemID, 15);
		acceptableGifts.put(Item.hoeDiamond.itemID, 15);
		acceptableGifts.put(Item.pickaxeDiamond.itemID, 15);
		acceptableGifts.put(Item.helmetDiamond.itemID, 15);
		acceptableGifts.put(Item.plateDiamond.itemID, 15);
		acceptableGifts.put(Item.legsDiamond.itemID, 15);
		acceptableGifts.put(Item.bootsDiamond.itemID, 15);
		acceptableGifts.put(Item.painting.itemID, 6);
		acceptableGifts.put(Item.enderPearl.itemID, 5);
		acceptableGifts.put(Item.eyeOfEnder.itemID, 10);
		acceptableGifts.put(Item.potion.itemID, 3);
		acceptableGifts.put(Item.slimeBall.itemID, 3);
		acceptableGifts.put(Item.saddle.itemID, 5);
		acceptableGifts.put(Item.gunpowder.itemID, 7);
		acceptableGifts.put(Item.appleGold.itemID, 25);
		acceptableGifts.put(Item.record11.itemID, 15);
		acceptableGifts.put(Item.record13.itemID, 15);
		acceptableGifts.put(Item.recordBlocks.itemID, 15);
		acceptableGifts.put(Item.recordCat.itemID, 15);
		acceptableGifts.put(Item.recordChirp.itemID, 15);
		acceptableGifts.put(Item.recordFar.itemID, 15);
		acceptableGifts.put(Item.recordMall.itemID, 15);
		acceptableGifts.put(Item.recordMellohi.itemID, 15);
		acceptableGifts.put(Item.recordStal.itemID, 15);
		acceptableGifts.put(Item.recordStrad.itemID, 15);
		acceptableGifts.put(Item.recordWard.itemID, 15);
		acceptableGifts.put(Item.emerald.itemID, 25);

		acceptableGifts.put(Block.plantRed.blockID, 3);
		acceptableGifts.put(Block.plantYellow.blockID, 3);
		acceptableGifts.put(Block.planks.blockID, 5);
		acceptableGifts.put(Block.wood.blockID, 3);

		acceptableGifts.put(Block.pumpkin.blockID, 3);
		acceptableGifts.put(Block.chest.blockID, 5);
		acceptableGifts.put(Block.cloth.blockID, 2);
		acceptableGifts.put(Block.oreIron.blockID, 4);
		acceptableGifts.put(Block.oreGold.blockID, 7);
		acceptableGifts.put(Block.oreRedstone.blockID, 3);
		acceptableGifts.put(Block.rail.blockID, 3);
		acceptableGifts.put(Block.railDetector.blockID, 5);
		acceptableGifts.put(Block.railPowered.blockID, 5);
		acceptableGifts.put(Block.furnaceIdle.blockID, 5);
		acceptableGifts.put(Block.workbench.blockID, 5);
		acceptableGifts.put(Block.blockLapis.blockID, 15);

		acceptableGifts.put(Block.bookShelf.blockID, 7);
		acceptableGifts.put(Block.blockGold.blockID, 50);
		acceptableGifts.put(Block.blockIron.blockID, 25);
		acceptableGifts.put(Block.blockDiamond.blockID, 100);
		acceptableGifts.put(Block.brewingStand.blockID, 12);
		acceptableGifts.put(Block.enchantmentTable.blockID, 25);
		acceptableGifts.put(Block.brick.blockID, 15);
		acceptableGifts.put(Block.obsidian.blockID, 15);
		acceptableGifts.put(Block.pistonBase.blockID, 10);
		acceptableGifts.put(Block.glowStone.blockID, 10);

		acceptableGifts.put(Block.blockEmerald.blockID, 100);
	}
}
